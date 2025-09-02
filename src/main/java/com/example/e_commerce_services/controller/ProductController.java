package com.example.e_commerce_services.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.dto.ProductDetailDto;
import com.example.e_commerce_services.dto.ProductListItemDto;
import com.example.e_commerce_services.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // /products?category=ao-thun
    @GetMapping
    public List<ProductListItemDto> listByCategory(@RequestParam("category") String categorySlug) {
        return service.listByCategorySlug(categorySlug);
    }

    @GetMapping("/{slug}")
    public ProductDetailDto getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }
}
