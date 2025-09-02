-- SCHEMA INIT (PostgreSQL)
-- Khuyến nghị: chạy trong schema public mặc định.
-- Tất cả thời gian dùng TIMESTAMPTZ.
-- ========== USERS & AUTH ==========
CREATE TABLE
    IF NOT EXISTS users (
        id BIGSERIAL PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        name VARCHAR(120),
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE TABLE
    IF NOT EXISTS roles (
        id SMALLSERIAL PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE
    IF NOT EXISTS user_roles (
        user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
        role_id SMALLINT NOT NULL REFERENCES roles (id) ON DELETE RESTRICT,
        PRIMARY KEY (user_id, role_id)
    );

-- Refresh token (tuỳ chọn nhưng hữu ích khi dùng rotation)
CREATE TABLE
    IF NOT EXISTS refresh_tokens (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
        token VARCHAR(512) NOT NULL UNIQUE,
        expires_at TIMESTAMPTZ NOT NULL,
        revoked BOOLEAN NOT NULL DEFAULT FALSE
    );

CREATE INDEX IF NOT EXISTS idx_refresh_user_expires ON refresh_tokens (user_id, expires_at);

-- ========== CATALOG ==========
CREATE TABLE
    IF NOT EXISTS categories (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(120) NOT NULL,
        slug VARCHAR(160) NOT NULL UNIQUE,
        parent_id BIGINT REFERENCES categories (id) ON DELETE SET NULL,
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE INDEX IF NOT EXISTS idx_cat_parent ON categories (parent_id);

CREATE TABLE
    IF NOT EXISTS products (
        id BIGSERIAL PRIMARY KEY,
        category_id BIGINT NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
        title VARCHAR(200) NOT NULL,
        slug VARCHAR(200) NOT NULL UNIQUE,
        description TEXT,
        brand VARCHAR(120),
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);

CREATE TABLE
    IF NOT EXISTS product_images (
        id BIGSERIAL PRIMARY KEY,
        product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
        url TEXT NOT NULL,
        position INT NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_images_product ON product_images (product_id);

-- Mỗi biến thể = 1 SKU
CREATE TABLE
    IF NOT EXISTS variants (
        id BIGSERIAL PRIMARY KEY,
        product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
        sku VARCHAR(64) NOT NULL UNIQUE,
        price INTEGER NOT NULL CHECK (price >= 0), -- VND
        stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
        attrs JSONB NOT NULL, -- {"color":"black","size":"M"}
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE INDEX IF NOT EXISTS idx_variants_product ON variants (product_id);

CREATE INDEX IF NOT EXISTS idx_variants_attrs_gin ON variants USING GIN (attrs);

-- ========== CART ==========
CREATE TABLE
    IF NOT EXISTS carts (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT UNIQUE NOT NULL REFERENCES users (id) ON DELETE CASCADE,
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE TABLE
    IF NOT EXISTS cart_items (
        id BIGSERIAL PRIMARY KEY,
        cart_id BIGINT NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
        variant_id BIGINT NOT NULL REFERENCES variants (id) ON DELETE RESTRICT,
        qty INT NOT NULL CHECK (qty > 0),
        price_snapshot INTEGER NOT NULL,
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now (),
        UNIQUE (cart_id, variant_id)
    );

-- ========== ORDERS ==========
CREATE TABLE
    IF NOT EXISTS orders (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
        number VARCHAR(40) NOT NULL UNIQUE,
        status VARCHAR(20) NOT NULL CHECK (
            status IN (
                'PLACED',
                'PROCESSING',
                'SHIPPED',
                'DELIVERED',
                'CANCELLED'
            )
        ),
        ship_full_name VARCHAR(120) NOT NULL,
        ship_phone VARCHAR(30) NOT NULL,
        ship_line1 VARCHAR(255) NOT NULL,
        ship_province VARCHAR(120) NOT NULL,
        ship_district VARCHAR(120) NOT NULL,
        ship_ward VARCHAR(120) NOT NULL,
        shipping_fee INTEGER NOT NULL DEFAULT 0,
        subtotal INTEGER NOT NULL,
        total INTEGER NOT NULL,
        created_at TIMESTAMPTZ DEFAULT now (),
        updated_at TIMESTAMPTZ DEFAULT now ()
    );

CREATE INDEX IF NOT EXISTS idx_orders_user_created ON orders (user_id, created_at DESC);

CREATE TABLE
    IF NOT EXISTS order_items (
        id BIGSERIAL PRIMARY KEY,
        order_id BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
        variant_id BIGINT NOT NULL REFERENCES variants (id) ON DELETE RESTRICT,
        sku VARCHAR(64) NOT NULL,
        title VARCHAR(200) NOT NULL,
        qty INT NOT NULL CHECK (qty > 0),
        unit_price INTEGER NOT NULL,
        line_total INTEGER NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items (order_id);