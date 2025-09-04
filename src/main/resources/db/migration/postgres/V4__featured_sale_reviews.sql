-- FEATURED
ALTER TABLE products
ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_products_featured ON products (is_featured);

-- ON SALE (ở cấp variant)
ALTER TABLE variants
ADD COLUMN IF NOT EXISTS sale_price INTEGER,
ADD COLUMN IF NOT EXISTS sale_start TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS sale_end TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_variants_sale ON variants (sale_start, sale_end);

-- REVIEWS + SUMMARY (cho top-rated)
CREATE TABLE
    IF NOT EXISTS product_reviews (
        id BIGSERIAL PRIMARY KEY,
        product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
        user_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
        rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
        comment TEXT,
        created_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE INDEX IF NOT EXISTS idx_reviews_product ON product_reviews (product_id);

CREATE TABLE
    IF NOT EXISTS product_rating_summary (
        product_id BIGINT PRIMARY KEY REFERENCES products (id) ON DELETE CASCADE,
        rating_avg NUMERIC(3, 2) NOT NULL DEFAULT 0,
        rating_count INT NOT NULL DEFAULT 0,
        updated_at TIMESTAMPTZ DEFAULT now ()
    );