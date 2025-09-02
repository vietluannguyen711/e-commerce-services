package com.example.e_commerce_services.service;

import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.e_commerce_services.domain.RefreshToken;
import com.example.e_commerce_services.domain.Role;
import com.example.e_commerce_services.domain.User;
import com.example.e_commerce_services.dto.LoginRequest;
import com.example.e_commerce_services.dto.RegisterRequest;
import com.example.e_commerce_services.dto.TokenResponse;
import com.example.e_commerce_services.exception.DuplicateEmailException;
import com.example.e_commerce_services.exception.NotFoundException;
import com.example.e_commerce_services.exception.UnauthenticatedException;
import com.example.e_commerce_services.repository.RefreshTokenRepository;
import com.example.e_commerce_services.repository.RoleRepository;
import com.example.e_commerce_services.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, RoleRepository roleRepo, RefreshTokenRepository refreshRepo,
            PasswordEncoder passwordEncoder, AuthenticationManager authManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.refreshRepo = refreshRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new DuplicateEmailException("Email đã tồn tại");
        }
        User u = new User();
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setName(req.name());
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new NotFoundException("ROLE_USER missing"));
        u.setRoles(Set.of(userRole));
        userRepo.save(u);
    }

    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        // Lấy user
        User u = userRepo.findByEmail(req.email()).orElseThrow(() -> new UnauthenticatedException("Sai thông tin đăng nhập"));
        // Tạo tokens
        String access = jwtService.generateAccessToken(u.getId(), u.getEmail());
        String refresh = jwtService.generateRefreshToken(u.getId(), u.getEmail());
        // Lưu refresh DB (rotation-friendly)
        persistRefresh(u, refresh);
        return new TokenResponse(access, refresh);
    }

    public TokenResponse refresh(String refreshToken) {
        var parsed = jwtService.parse(refreshToken).getBody();
        if (!"refresh".equals(parsed.getAudience())) {
            throw new UnauthenticatedException("Token không hợp lệ");
        }
        // Kiểm tra tồn tại & chưa revoke
        RefreshToken rt = refreshRepo.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new UnauthenticatedException("Refresh token không hợp lệ"));
        if (rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthenticatedException("Refresh token đã hết hạn");
        }
        User u = rt.getUser();
        // Rotation: revoke token cũ, cấp token mới
        rt.setRevoked(true);
        String newAccess = jwtService.generateAccessToken(u.getId(), u.getEmail());
        String newRefresh = jwtService.generateRefreshToken(u.getId(), u.getEmail());
        persistRefresh(u, newRefresh);
        return new TokenResponse(newAccess, newRefresh);
    }

    private void persistRefresh(User u, String token) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(u);
        rt.setToken(token);
        rt.setExpiresAt(OffsetDateTime.from(jwtService.parse(token).getBody().getExpiration().toInstant().atOffset(OffsetDateTime.now().getOffset())));
        rt.setRevoked(false);
        refreshRepo.save(rt);
        // dọn dẹp refresh cũ đã hết hạn (best-effort)
        refreshRepo.deleteByExpiresAtBefore(OffsetDateTime.now().minusDays(1));
    }
}
