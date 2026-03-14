-- Flyway V1: スキーマ・テーブル定義
-- もともとの `schema.sql` の内容を移行

-- アプリケーション用スキーマ
CREATE SCHEMA IF NOT EXISTS app;
SET search_path TO app;

-- ユーザーマスタ
CREATE TABLE IF NOT EXISTS m_user (
    user_id         UUID PRIMARY KEY,
    user_name       VARCHAR(50) NOT NULL,
    password        VARCHAR(100) NOT NULL,
    role            SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(50),
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(50),
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_name UNIQUE (user_name)
);
CREATE INDEX IF NOT EXISTS idx_user_name ON m_user(user_name);
CREATE INDEX IF NOT EXISTS idx_user_role ON m_user(role);
COMMENT ON TABLE m_user IS 'ユーザーマスタ';

-- 顧客マスタ
CREATE TABLE IF NOT EXISTS m_customer (
    customer_id     UUID PRIMARY KEY,
    customer_number VARCHAR(10) NOT NULL,
    customer_name   VARCHAR(50) NOT NULL,
    address         VARCHAR(100),
    phone_number    VARCHAR(20),
    email           VARCHAR(50),
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     UUID,
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     UUID,
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customer_number UNIQUE (customer_number)
);
CREATE INDEX IF NOT EXISTS idx_customer_name ON m_customer(customer_name);
CREATE INDEX IF NOT EXISTS idx_customer_address ON m_customer(address);
CREATE INDEX IF NOT EXISTS idx_customer_phone ON m_customer(phone_number);
CREATE INDEX IF NOT EXISTS idx_customer_email ON m_customer(email);
COMMENT ON TABLE m_customer IS '顧客マスタ';

-- 商品マスタ
CREATE TABLE IF NOT EXISTS m_product (
    product_id      UUID PRIMARY KEY,
    product_number  VARCHAR(10) NOT NULL,
    product_name    VARCHAR(50) NOT NULL,
    description     VARCHAR(100),
    price           INT NOT NULL,
    category        SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     UUID,
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     UUID,
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_product_number UNIQUE (product_number)
);
CREATE INDEX IF NOT EXISTS idx_product_name ON m_product(product_name);
CREATE INDEX IF NOT EXISTS idx_product_price ON m_product(price);
COMMENT ON TABLE m_product IS '商品マスタ';

-- 在庫マスタ
CREATE TABLE IF NOT EXISTS m_stock (
    stock_id        UUID PRIMARY KEY,
    product_id      UUID NOT NULL,
    quantity        INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     UUID,
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     UUID,
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_stock_product UNIQUE (product_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES m_product(product_id)
);
CREATE INDEX IF NOT EXISTS idx_stock_product ON m_stock(product_id);
COMMENT ON TABLE m_stock IS '在庫マスタ';

-- 注文トランザクション
CREATE TABLE IF NOT EXISTS t_order (
    order_id        UUID PRIMARY KEY,
    customer_id     UUID NOT NULL,
    order_date      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     UUID,
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     UUID,
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES m_customer(customer_id)
);
CREATE INDEX IF NOT EXISTS idx_order_customer ON t_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_date ON t_order(order_date);
COMMENT ON TABLE t_order IS '注文トランザクション';

-- 注文明細トランザクション
CREATE TABLE IF NOT EXISTS t_order_item (
    order_item_id   UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    product_id      UUID NOT NULL,
    quantity        INT NOT NULL,
    unit_price      INT NOT NULL,
    create_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES t_order(order_id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES m_product(product_id)
);
CREATE INDEX IF NOT EXISTS idx_order_item_order ON t_order_item(order_id);
COMMENT ON TABLE t_order_item IS '注文明細トランザクション';

