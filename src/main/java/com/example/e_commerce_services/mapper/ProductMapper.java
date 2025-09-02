package com.example.e_commerce_services.mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.e_commerce_services.domain.Product;
import com.example.e_commerce_services.domain.ProductImage;
import com.example.e_commerce_services.domain.Variant;
import com.example.e_commerce_services.dto.ProductDetailDto;
import com.example.e_commerce_services.dto.ProductListItemDto;
import com.example.e_commerce_services.dto.VariantDto;

public class ProductMapper {

    public static ProductListItemDto toListItem(Product p) {
        String thumb = p.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition))
                .map(ProductImage::getUrl)
                .findFirst().orElse(null);

        // min/max price từ variants (đơn giản cho MVP)
        Integer min = p.getVariants().stream().map(Variant::getPrice).min(Integer::compareTo).orElse(0);
        Integer max = p.getVariants().stream().map(Variant::getPrice).max(Integer::compareTo).orElse(0);

        return new ProductListItemDto(
                p.getId(), p.getSlug(), p.getTitle(), thumb, min, max
        );
        // Nếu sau này muốn tối ưu N+1, có thể chuyển qua projection/native query ở repo.
    }

    public static ProductDetailDto toDetail(Product p) {
        List<String> imgs = p.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getPosition))
                .map(ProductImage::getUrl)
                .toList();

        // gom thuộc tính từ tất cả variants (tập giá trị duy nhất)
        Map<String, Set<String>> attrSets = new HashMap<>();
        for (Variant v : p.getVariants()) {
            v.getAttrs().forEach((k, val) -> {
                attrSets.computeIfAbsent(k, __ -> new LinkedHashSet<>()).add(val);
            });
        }
        Map<String, List<String>> attributes = attrSets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

        List<VariantDto> vs = p.getVariants().stream()
                .map(v -> new VariantDto(v.getSku(), v.getPrice(), v.getStock(), v.getAttrs()))
                .toList();

        return new ProductDetailDto(
                p.getId(), p.getSlug(), p.getTitle(), p.getDescription(), p.getBrand(),
                imgs, attributes, vs
        );
    }
}
