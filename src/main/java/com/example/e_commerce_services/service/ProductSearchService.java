package com.example.e_commerce_services.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.e_commerce_services.dto.ProductListItemDto;
import com.example.e_commerce_services.util.AttrParser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class ProductSearchService {

    private final EntityManager em;

    public ProductSearchService(EntityManager em) {
        this.em = em;
    }

    public Page<ProductListItemDto> search(String categorySlug,
            String q,
            Integer priceMin,
            Integer priceMax,
            String attrs,
            Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        // JOINs
        String joins = """
            FROM products p
            JOIN categories c ON c.id = p.category_id
            LEFT JOIN LATERAL (
              SELECT MIN(v.price) AS min_price, MAX(v.price) AS max_price
              FROM variants v
              WHERE v.product_id = p.id
            ) agg ON true
            """;

        // Base filter for selecting product ids (we will also need a join with variants for filters)
        // Build filter conditions using variants table when needed
        List<String> variantConds = new ArrayList<>();

        if (StringUtils.hasText(categorySlug)) {
            where.append(" AND c.slug = :categorySlug ");
            params.put("categorySlug", categorySlug);
        }
        if (StringUtils.hasText(q)) {
            where.append(" AND p.title ILIKE :q ");
            params.put("q", "%" + q.trim() + "%");
        }
        if (priceMin != null) {
            variantConds.add(" v.price >= :priceMin ");
            params.put("priceMin", priceMin);
        }
        if (priceMax != null) {
            variantConds.add(" v.price <= :priceMax ");
            params.put("priceMax", priceMax);
        }
        String attrsJson = AttrParser.toJson(AttrParser.parse(attrs));
        if (!"{}".equals(attrsJson)) {
            variantConds.add(" v.attrs @> CAST(:attrs AS jsonb) ");
            params.put("attrs", attrsJson);
        }

        // If there are variant conditions, ensure there exists a variant matching them (semi-join)
        if (!variantConds.isEmpty()) {
            where.append(" AND EXISTS (SELECT 1 FROM variants v WHERE v.product_id = p.id AND ");
            where.append(String.join(" AND ", variantConds));
            where.append(") ");
        }

        // Build ORDER BY
        String orderBy = " ORDER BY p.created_at DESC ";
        if (pageable.getSort().isSorted()) {
            // Allow using Spring's sort mapping (optional)
        } else if (pageable instanceof PageRequest && pageable.getSort().isEmpty()) {
            // Use provided 'sort' param externally; we'll handle in controller.
        }

        // We will set orderBy in controller according to 'sort' param
        // but give default here; controller can override by passing custom PageRequest with Sort.unsorted()
        // Count query
        String countSql = "SELECT COUNT(*) " + joins + where;
        Query countQ = em.createNativeQuery(countSql);
        params.forEach(countQ::setParameter);
        Number total = (Number) countQ.getSingleResult();
        if (total.intValue() == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Data query: fetch list items with min/max price and thumbnail
        String dataSql = """
            SELECT
              p.id,
              p.slug,
              p.title,
              (SELECT url FROM product_images img WHERE img.product_id = p.id ORDER BY img.position ASC LIMIT 1) AS thumbnail,
              agg.min_price,
              agg.max_price,
              p.created_at
            """ + joins + where + orderBy
                + " LIMIT :limit OFFSET :offset";

        Query dataQ = em.createNativeQuery(dataSql);
        params.forEach(dataQ::setParameter);
        dataQ.setParameter("limit", pageable.getPageSize());
        dataQ.setParameter("offset", (int) pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        List<ProductListItemDto> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String slug = (String) r[1];
            String title = (String) r[2];
            String thumbnail = (String) r[3];
            Integer minPrice = r[4] == null ? 0 : ((Number) r[4]).intValue();
            Integer maxPrice = r[5] == null ? 0 : ((Number) r[5]).intValue();
            items.add(new ProductListItemDto(id, slug, title, thumbnail, minPrice, maxPrice));
        }
        return new PageImpl<>(items, pageable, total.longValue());
    }

    // Hỗ trợ ORDER BY custom
    public String resolveOrderBy(String sortParam) {
        if ("price_asc".equalsIgnoreCase(sortParam)) {
            return " ORDER BY agg.min_price ASC NULLS LAST ";
        }
        if ("price_desc".equalsIgnoreCase(sortParam)) {
            return " ORDER BY agg.min_price DESC NULLS LAST ";
        }
        if ("newest".equalsIgnoreCase(sortParam) || sortParam == null || sortParam.isBlank()) {
            return " ORDER BY p.created_at DESC ";
        }
        return " ORDER BY p.created_at DESC ";
    }

    public Page<ProductListItemDto> searchWithSort(String categorySlug, String q,
            Integer priceMin, Integer priceMax,
            String attrs, String sort, Pageable pageable) {
        // HACK gọn: dùng reflect nhỏ để thay orderBy ở runtime
        String orderBy = resolveOrderBy(sort);
        return searchInternal(categorySlug, q, priceMin, priceMax, attrs, pageable, orderBy);
    }

    private Page<ProductListItemDto> searchInternal(String categorySlug, String q,
            Integer priceMin, Integer priceMax,
            String attrs, Pageable pageable,
            String orderBy) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        String joins = """
        FROM products p
        JOIN categories c ON c.id = p.category_id
        LEFT JOIN LATERAL (
          SELECT MIN(v.price) AS min_price, MAX(v.price) AS max_price
          FROM variants v
          WHERE v.product_id = p.id
        ) agg ON true
        """;

        List<String> variantConds = new ArrayList<>();
        if (org.springframework.util.StringUtils.hasText(categorySlug)) {
            where.append(" AND c.slug = :categorySlug ");
            params.put("categorySlug", categorySlug);
        }
        if (org.springframework.util.StringUtils.hasText(q)) {
            where.append(" AND p.title ILIKE :q ");
            params.put("q", "%" + q.trim() + "%");
        }
        if (priceMin != null) {
            variantConds.add(" v.price >= :priceMin ");
            params.put("priceMin", priceMin);
        }
        if (priceMax != null) {
            variantConds.add(" v.price <= :priceMax ");
            params.put("priceMax", priceMax);
        }
        String attrsJson = AttrParser.toJson(AttrParser.parse(attrs));
        if (!"{}".equals(attrsJson)) {
            variantConds.add(" v.attrs @> CAST(:attrs AS jsonb) ");
            params.put("attrs", attrsJson);
        }

        if (!variantConds.isEmpty()) {
            where.append(" AND EXISTS (SELECT 1 FROM variants v WHERE v.product_id = p.id AND ");
            where.append(String.join(" AND ", variantConds));
            where.append(") ");
        }

        String countSql = "SELECT COUNT(*) " + joins + where;
        var countQ = em.createNativeQuery(countSql);
        params.forEach(countQ::setParameter);
        Number total = (Number) countQ.getSingleResult();
        if (total.intValue() == 0) {
            return new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
        }

        String dataSql = """
        SELECT
          p.id,
          p.slug,
          p.title,
          (SELECT url FROM product_images img WHERE img.product_id = p.id ORDER BY img.position ASC LIMIT 1) AS thumbnail,
          agg.min_price,
          agg.max_price
        """ + joins + where + orderBy
                + " LIMIT :limit OFFSET :offset";

        var dataQ = em.createNativeQuery(dataSql);
        params.forEach(dataQ::setParameter);
        dataQ.setParameter("limit", pageable.getPageSize());
        dataQ.setParameter("offset", (int) pageable.getOffset());

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = dataQ.getResultList();
        java.util.List<ProductListItemDto> items = new java.util.ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String slug = (String) r[1];
            String title = (String) r[2];
            String thumbnail = (String) r[3];
            Integer minPrice = r[4] == null ? 0 : ((Number) r[4]).intValue();
            Integer maxPrice = r[5] == null ? 0 : ((Number) r[5]).intValue();
            items.add(new ProductListItemDto(id, slug, title, thumbnail, minPrice, maxPrice));
        }
        return new org.springframework.data.domain.PageImpl<>(items, pageable, total.longValue());
    }
}
