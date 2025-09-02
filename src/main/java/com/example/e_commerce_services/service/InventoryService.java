package com.example.e_commerce_services.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_services.domain.Variant;
import com.example.e_commerce_services.dto.VariantDto;
import com.example.e_commerce_services.exception.SkuNotFoundException;
import com.example.e_commerce_services.repository.VariantRepository;

@Service
public class InventoryService {

    private final VariantRepository variantRepo;

    public InventoryService(VariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }

    @Transactional(readOnly = true)
    public VariantDto checkAvailability(String productSlug, Map<String, String> attrs) {
        String json = toJson(attrs);
        Variant v = variantRepo.findByProductSlugAndAttrs(productSlug, json)
                .orElseThrow(() -> new SkuNotFoundException(
                "Không tìm thấy SKU cho sản phẩm '" + productSlug + "' với thuộc tính đã chọn"));
        return new VariantDto(v.getSku(), v.getPrice(), v.getStock(), v.getAttrs());
    }

    private String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
