package com.example.e_commerce_services.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_services.domain.Category;
import com.example.e_commerce_services.domain.Product;
import com.example.e_commerce_services.dto.ProductDetailDto;
import com.example.e_commerce_services.dto.ProductListItemDto;
import com.example.e_commerce_services.exception.NotFoundException;
import com.example.e_commerce_services.mapper.ProductMapper;
import com.example.e_commerce_services.repository.CategoryRepository;
import com.example.e_commerce_services.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional(readOnly = true)
    public List<ProductListItemDto> listByCategorySlug(String categorySlug) {
        Category c = categoryRepo.findBySlug(categorySlug)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categorySlug));

        List<Product> products = productRepo.findAllByCategory_Id(c.getId());
        return products.stream().map(ProductMapper::toListItem).toList();
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getBySlug(String slug) {
        Product p = productRepo.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + slug));
        return ProductMapper.toDetail(p);
    }
}
