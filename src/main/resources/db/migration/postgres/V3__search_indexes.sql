-- V3__search_indexes.sql
-- Bật extension pg_trgm (chỉ cần 1 lần cho database này)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Index tăng tốc search theo title với ILIKE
CREATE INDEX IF NOT EXISTS idx_products_title_trgm ON products USING GIN (title gin_trgm_ops);

-- Index JSONB attrs (nếu chưa có)
CREATE INDEX IF NOT EXISTS idx_variants_attrs_gin ON variants USING GIN (attrs);

-- Index lọc theo giá
CREATE INDEX IF NOT EXISTS idx_variants_product_price ON variants (product_id, price);