package com.example.e_commerce_services.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_services.service.ProductImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService service;

    // DTO
    public record ProductImageDto(Long id, String url, String fileName, boolean isMain) {

    }

    @GetMapping
    public List<Map<String, Object>> list(@PathVariable Long productId) {
        return service.list(productId).stream().map(i -> Map.of(
                "id", (Object) i.getId(), "url", (Object) i.getUrl(), "fileName", (Object) i.getFileName(), "isMain", (Object) i.isMain()
        )).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, Object>> upload(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        var rs = new ArrayList<Map<String, Object>>();
        for (var f : files) {
            var i = service.upload(productId, f);
            rs.add(Map.of("id", i.getId(), "url", i.getUrl(), "fileName", i.getFileName(), "isMain", i.isMain()));
        }
        return rs;
    }

    @PutMapping("/{imageId}/set-main")
    public void setMain(@PathVariable Long productId, @PathVariable Long imageId) {
        service.setMain(productId, imageId);
    }

    @DeleteMapping("/{imageId}")
    public void delete(@PathVariable Long productId, @PathVariable Long imageId) throws IOException {
        service.delete(productId, imageId);
    }
}
