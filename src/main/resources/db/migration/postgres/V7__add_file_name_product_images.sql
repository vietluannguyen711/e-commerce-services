-- 1) Thêm cột mới (tạm thời cho phép NULL để backfill)
ALTER TABLE product_images
ADD COLUMN IF NOT EXISTS file_name VARCHAR(512);