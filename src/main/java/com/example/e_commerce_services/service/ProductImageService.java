package com.example.e_commerce_services.service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_services.domain.ProductImage;
import com.example.e_commerce_services.repository.ProductImageRepository;
import com.example.e_commerce_services.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageService {

    @Value("${app.upload.dir}")
    String uploadDir;
    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;

    public ProductImage upload(Long productId, MultipartFile file) throws IOException {
        var product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        var ct = Optional.ofNullable(file.getContentType()).orElse("");
        if (!ct.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files");
        }

        var now = LocalDate.now();
        var sub = Paths.get(String.valueOf(now.getYear()), String.format("%02d", now.getMonthValue()));
        var folder = Paths.get(uploadDir).resolve(sub);
        Files.createDirectories(folder);

        var ext = Optional.ofNullable(FilenameUtils.getExtension(file.getOriginalFilename()))
                .filter(s -> !s.isBlank()).map(String::toLowerCase).orElse("jpg");
        var fname = UUID.randomUUID() + "." + ext;

        var path = folder.resolve(fname);
        file.transferTo(path.toFile());

        var url = "/uploads/" + sub.toString().replace("\\", "/") + "/" + fname;

        var img = new ProductImage();
        img.setProduct(product);
        img.setFileName(fname);
        img.setUrl(url);

        if (imageRepo.findByProductIdOrderByIsMainDescCreatedAtDesc(productId).isEmpty()) {
            img.setMain(true);
        }
        return imageRepo.save(img);
    }

    public List<ProductImage> list(Long productId) {
        return imageRepo.findByProductIdOrderByIsMainDescCreatedAtDesc(productId);
    }

    public void setMain(Long productId, Long imageId) {
        var img = imageRepo.findById(imageId).orElseThrow();
        if (!img.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Mismatch");
        }
        img.setMain(true);
        imageRepo.unsetOthersExcept(productId, imageId);
    }

    public void delete(Long productId, Long imageId) throws IOException {
        var img = imageRepo.findById(imageId).orElseThrow();
        if (!img.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Mismatch");
        }
        // (Có thể tính đúng đường dẫn file từ url nếu bạn lưu cả subfolder)
        imageRepo.delete(img);
    }
}
