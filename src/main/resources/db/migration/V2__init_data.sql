-- Flyway V2: 初期データ投入
-- もともとの `data.sql` の内容を移行

SET search_path TO app;

-- ユーザー（role: 1=一般, 2=システム管理者）
INSERT INTO m_user (user_id, user_name, password, role, delete_flag)
VALUES
    ('11111111-1111-1111-1111-111111111101'::uuid, 'admin', '{noop}admin', 2, false),
    ('11111111-1111-1111-1111-111111111102'::uuid, 'user1', '{noop}user1', 1, false)
ON CONFLICT (user_id) DO NOTHING;

-- 顧客
INSERT INTO m_customer (customer_id, customer_number, customer_name, address, phone_number, email, delete_flag)
VALUES
    ('22222222-2222-2222-2222-222222222201'::uuid, 'C000000001', 'サンプル顧客A', '東京都渋谷区', '090-1234-5678', 'customer-a@example.com', false),
    ('22222222-2222-2222-2222-222222222202'::uuid, 'C000000002', 'サンプル顧客B', '大阪府大阪市', '080-9876-5432', 'customer-b@example.com', false)
ON CONFLICT (customer_id) DO NOTHING;

-- 商品（category: 1）
INSERT INTO m_product (product_id, product_number, product_name, description, price, category, delete_flag)
VALUES
    ('33333333-3333-3333-3333-333333333301'::uuid, 'P000000001', '商品A', '説明A', 1000, 1, false),
    ('33333333-3333-3333-3333-333333333302'::uuid, 'P000000002', '商品B', '説明B', 2000, 1, false),
    ('33333333-3333-3333-3333-333333333303'::uuid, 'P000000003', '商品C', '説明C', 1500, 1, false)
ON CONFLICT (product_id) DO NOTHING;

-- 在庫（status: 0=利用可能）
INSERT INTO m_stock (stock_id, product_id, quantity, status, delete_flag)
VALUES
    ('44444444-4444-4444-4444-444444444401'::uuid, '33333333-3333-3333-3333-333333333301'::uuid, 100, 0, false),
    ('44444444-4444-4444-4444-444444444402'::uuid, '33333333-3333-3333-3333-333333333302'::uuid, 50, 0, false),
    ('44444444-4444-4444-4444-444444444403'::uuid, '33333333-3333-3333-3333-333333333303'::uuid, 80, 0, false)
ON CONFLICT (stock_id) DO NOTHING;

-- 注文（status: 1=受付）
INSERT INTO t_order (order_id, customer_id, order_date, status, delete_flag)
VALUES
    ('55555555-5555-5555-5555-555555555501'::uuid, '22222222-2222-2222-2222-222222222201'::uuid, CURRENT_TIMESTAMP, 1, false),
    ('55555555-5555-5555-5555-555555555502'::uuid, '22222222-2222-2222-2222-222222222202'::uuid, CURRENT_TIMESTAMP, 1, false)
ON CONFLICT (order_id) DO NOTHING;

-- 注文明細
INSERT INTO t_order_item (order_item_id, order_id, product_id, quantity, unit_price)
VALUES
    ('66666666-6666-6666-6666-666666666601'::uuid, '55555555-5555-5555-5555-555555555501'::uuid, '33333333-3333-3333-3333-333333333301'::uuid, 2, 1000),
    ('66666666-6666-6666-6666-666666666602'::uuid, '55555555-5555-5555-5555-555555555501'::uuid, '33333333-3333-3333-3333-333333333302'::uuid, 1, 2000),
    ('66666666-6666-6666-6666-666666666603'::uuid, '55555555-5555-5555-5555-555555555502'::uuid, '33333333-3333-3333-3333-333333333303'::uuid, 3, 1500)
ON CONFLICT (order_item_id) DO NOTHING;

