package com.example.e_commerce_services.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.dto.ProductDetailDto;
import com.example.e_commerce_services.dto.ProductListItemDto;
import com.example.e_commerce_services.service.ProductSearchService;
import com.example.e_commerce_services.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final ProductSearchService searchService;

    public ProductController(ProductService service, ProductSearchService searchService) {
        this.service = service;
        this.searchService = searchService;
    }

    // /products?category=ao-thun
    // @GetMapping
    // public List<ProductListItemDto> listByCategory(@RequestParam("category") String categorySlug) {
    //     return service.listByCategorySlug(categorySlug);
    // }

    @GetMapping("/{slug}")
    public ProductDetailDto getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @GetMapping
    public Page<ProductListItemDto> search(
            @RequestParam(value = "category", required = false) String categorySlug,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "priceMin", required = false) Integer priceMin,
            @RequestParam(value = "priceMax", required = false) Integer priceMax,
            @RequestParam(value = "attrs", required = false) String attrs,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort
    ) {
        // Pageable của Spring giữ offset/limit; sort sẽ do service build ORDER BY
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());
        return searchService.searchWithSort(categorySlug, q, priceMin, priceMax, attrs, sort, pageable);
    }
}
