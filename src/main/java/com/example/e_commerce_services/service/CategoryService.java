package com.example.e_commerce_services.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.e_commerce_services.dto.CategoryDto;
import com.example.e_commerce_services.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<CategoryDto> listAll() {
        return categoryRepo.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }
}
