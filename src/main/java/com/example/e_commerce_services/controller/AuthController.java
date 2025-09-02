package com.example.e_commerce_services.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.domain.User;
import com.example.e_commerce_services.dto.LoginRequest;
import com.example.e_commerce_services.dto.MeResponse;
import com.example.e_commerce_services.dto.RegisterRequest;
import com.example.e_commerce_services.dto.TokenResponse;
import com.example.e_commerce_services.repository.UserRepository;
import com.example.e_commerce_services.security.UserPrincipal;
import com.example.e_commerce_services.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final UserRepository userRepo;

    public AuthController(AuthService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public Object register(@Valid @RequestBody RegisterRequest req) {
        service.register(req);
        return java.util.Map.of("userId", userRepo.findByEmail(req.email()).get().getId(), "email", req.email());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("refreshToken");
        return service.refresh(token);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserPrincipal user) {
        User u = userRepo.findById(user.getId()).orElseThrow();
        return new MeResponse(u.getId(), u.getEmail(), u.getName());
    }
}
