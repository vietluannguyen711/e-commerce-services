-- 1) Thêm cột mới (tạm thời cho phép NULL để backfill)
ALTER TABLE product_images
ADD COLUMN IF NOT EXISTS is_main BOOLEAN;

-- 2) Backfill: chọn ảnh "đầu tiên" cho mỗi product làm is_main=true
--    Ưu tiên position nhỏ nhất; nếu position NULL thì fallback theo id nhỏ nhất
WITH first_per_product AS (
  SELECT DISTINCT ON (product_id) id
  FROM product_images
  ORDER BY product_id, position NULLS LAST, id
)
UPDATE product_images pi
SET is_main = TRUE
FROM first_per_product f
WHERE pi.id = f.id;

-- 3) Set false cho các bản ghi còn lại (chưa gán)
UPDATE product_images
SET is_main = FALSE
WHERE is_main IS NULL;

-- 4) Ép NOT NULL + default
ALTER TABLE product_images
ALTER COLUMN is_main SET NOT NULL;

ALTER TABLE product_images
ALTER COLUMN is_main SET DEFAULT FALSE;

-- 5) Đảm bảo mỗi product chỉ có tối đa 1 ảnh main
--    Dùng unique partial index (chuẩn nhất trên Postgres)
CREATE UNIQUE INDEX IF NOT EXISTS ux_product_images_one_main_per_product
  ON product_images (product_id)
  WHERE is_main = TRUE;
