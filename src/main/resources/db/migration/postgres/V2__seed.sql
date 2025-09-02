-- ROLES
INSERT INTO
    roles (name)
VALUES
    ('ROLE_USER') ON CONFLICT DO NOTHING;

INSERT INTO
    roles (name)
VALUES
    ('ROLE_ADMIN') ON CONFLICT DO NOTHING;

-- USERS (LƯU Ý: thay password_hash bằng BCrypt thật từ BE)
-- Ví dụ: để trống, hoặc tạm lưu hash của "User@123" do BE tạo sau.
INSERT INTO
    users (email, password_hash, name)
VALUES
    (
        'admin@demo.vn',
        '$2a$10$replace_me_admin_hash',
        'Admin Demo'
    ),
    (
        'user@demo.vn',
        '$2a$10$replace_me_user__hash',
        'User Demo'
    ) ON CONFLICT (email) DO NOTHING;

-- Gán ROLE
INSERT INTO
    user_roles (user_id, role_id)
SELECT
    u.id,
    r.id
FROM
    users u
    JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE
    u.email = 'admin@demo.vn' ON CONFLICT DO NOTHING;

INSERT INTO
    user_roles (user_id, role_id)
SELECT
    u.id,
    r.id
FROM
    users u
    JOIN roles r ON r.name = 'ROLE_USER'
WHERE
    u.email = 'user@demo.vn' ON CONFLICT DO NOTHING;

-- CATEGORIES
INSERT INTO
    categories (name, slug, parent_id)
VALUES
    ('Áo thun', 'ao-thun', NULL),
    ('Sơ mi', 'so-mi', NULL),
    ('Quần', 'quan', NULL) ON CONFLICT (slug) DO NOTHING;

-- PRODUCTS
-- Áo thun Basic
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'ao-thun'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Áo thun Basic',
    'ao-thun-basic',
    'Cotton 100% thoáng mát',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- Áo thun Oversize
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'ao-thun'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Áo thun Oversize',
    'ao-thun-oversize',
    'Form rộng trẻ trung',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- Sơ mi Trơn
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'so-mi'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Sơ mi Trơn',
    'so-mi-tron',
    'Chất vải mát, ít nhăn',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- Sơ mi Kẻ
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'so-mi'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Sơ mi Kẻ',
    'so-mi-ke',
    'Họa tiết kẻ lịch sự',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- Quần Jeans Slim
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'quan'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Quần Jeans Slim',
    'quan-jeans-slim',
    'Jeans co giãn, ôm vừa',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- Quần Kaki
WITH
    c AS (
        SELECT
            id
        FROM
            categories
        WHERE
            slug = 'quan'
    )
INSERT INTO
    products (category_id, title, slug, description, brand)
SELECT
    c.id,
    'Quần Kaki',
    'quan-kaki',
    'Kaki đứng form',
    'LuanWear'
FROM
    c ON CONFLICT (slug) DO NOTHING;

-- IMAGES (chèn tối thiểu 2 ảnh / product)
INSERT INTO
    product_images (product_id, url, position)
SELECT
    p.id,
    'https://picsum.photos/seed/' || p.slug || '/800/800',
    0
FROM
    products p ON CONFLICT DO NOTHING;

INSERT INTO
    product_images (product_id, url, position)
SELECT
    p.id,
    'https://picsum.photos/seed/' || p.slug || '-2/800/800',
    1
FROM
    products p ON CONFLICT DO NOTHING;

-- VARIANTS (SKU + attrs + price + stock)
-- Áo thun Basic
-- VARIANTS (SKU + attrs + price + stock) — KHÔNG dùng CTE

-- Áo thun Basic
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-BLACK-M', 120000, 12, '{"color":"black","size":"M"}'::jsonb
FROM products WHERE slug='ao-thun-basic'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-BLACK-L', 120000, 0, '{"color":"black","size":"L"}'::jsonb
FROM products WHERE slug='ao-thun-basic'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-WHITE-L', 130000, 5, '{"color":"white","size":"L"}'::jsonb
FROM products WHERE slug='ao-thun-basic'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-BLUE-XL', 140000, 8, '{"color":"blue","size":"XL"}'::jsonb
FROM products WHERE slug='ao-thun-basic'
ON CONFLICT (sku) DO NOTHING;

-- Áo thun Oversize
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-OV-BLACK-L', 150000, 6, '{"color":"black","size":"L"}'::jsonb
FROM products WHERE slug='ao-thun-oversize'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'TSHIRT-OV-WHITE-L', 150000, 2, '{"color":"white","size":"L"}'::jsonb
FROM products WHERE slug='ao-thun-oversize'
ON CONFLICT (sku) DO NOTHING;

-- Sơ mi Trơn
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'SHIRT-PLAIN-WHITE-M', 220000, 10, '{"color":"white","size":"M"}'::jsonb
FROM products WHERE slug='so-mi-tron'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'SHIRT-PLAIN-BLUE-L', 230000, 0, '{"color":"blue","size":"L"}'::jsonb
FROM products WHERE slug='so-mi-tron'
ON CONFLICT (sku) DO NOTHING;

-- Sơ mi Kẻ
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'SHIRT-STRIPE-BLUE-M', 240000, 7, '{"color":"blue","size":"M"}'::jsonb
FROM products WHERE slug='so-mi-ke'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'SHIRT-STRIPE-BLACK-L', 250000, 4, '{"color":"black","size":"L"}'::jsonb
FROM products WHERE slug='so-mi-ke'
ON CONFLICT (sku) DO NOTHING;

-- Quần Jeans Slim
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'JEANS-SLIM-BLUE-30', 350000, 9, '{"color":"blue","size":"30"}'::jsonb
FROM products WHERE slug='quan-jeans-slim'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'JEANS-SLIM-BLUE-32', 350000, 0, '{"color":"blue","size":"32"}'::jsonb
FROM products WHERE slug='quan-jeans-slim'
ON CONFLICT (sku) DO NOTHING;

-- Quần Kaki
INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'KAKI-BEIGE-31', 320000, 11, '{"color":"beige","size":"31"}'::jsonb
FROM products WHERE slug='quan-kaki'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO variants (product_id, sku, price, stock, attrs)
SELECT id, 'KAKI-BEIGE-33', 320000, 3, '{"color":"beige","size":"33"}'::jsonb
FROM products WHERE slug='quan-kaki'
ON CONFLICT (sku) DO NOTHING;
