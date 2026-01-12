-- PostgreSQL用テーブル作成SQL
-- MVPテーブル設計に基づく

-- ユーザーマスタ
CREATE TABLE m_user (
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

-- ユーザーマスタのインデックス
CREATE INDEX idx_user_name ON m_user(user_name);
CREATE INDEX idx_user_role ON m_user(role);

-- ユーザーマスタのコメント
COMMENT ON TABLE m_user IS 'ユーザーマスタ';
COMMENT ON COLUMN m_user.user_id IS 'ユーザーID';
COMMENT ON COLUMN m_user.user_name IS 'ユーザー名';
COMMENT ON COLUMN m_user.password IS 'パスワード';
COMMENT ON COLUMN m_user.role IS 'ロール';
COMMENT ON COLUMN m_user.delete_flag IS '削除フラグ';


-- 顧客マスタ
CREATE TABLE m_customer (
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

-- 顧客マスタのインデックス
CREATE INDEX idx_customer_name ON m_customer(customer_name);
CREATE INDEX idx_customer_address ON m_customer(address);
CREATE INDEX idx_customer_phone ON m_customer(phone_number);
CREATE INDEX idx_customer_email ON m_customer(email);

-- 顧客マスタのコメント
COMMENT ON TABLE m_customer IS '顧客マスタ';
COMMENT ON COLUMN m_customer.customer_id IS '顧客ID';
COMMENT ON COLUMN m_customer.customer_number IS '顧客番号';
COMMENT ON COLUMN m_customer.customer_name IS '顧客名';
COMMENT ON COLUMN m_customer.address IS '住所';
COMMENT ON COLUMN m_customer.phone_number IS '電話番号';
COMMENT ON COLUMN m_customer.email IS 'メールアドレス';


-- 商品マスタ
CREATE TABLE m_product (
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

-- 商品マスタのインデックス
CREATE INDEX idx_product_name ON m_product(product_name);
CREATE INDEX idx_product_price ON m_product(price);

-- 商品マスタのコメント
COMMENT ON TABLE m_product IS '商品マスタ';
COMMENT ON COLUMN m_product.product_id IS '商品ID';
COMMENT ON COLUMN m_product.product_number IS '商品番号';
COMMENT ON COLUMN m_product.product_name IS '商品名';
COMMENT ON COLUMN m_product.description IS '説明';
COMMENT ON COLUMN m_product.price IS '価格';
COMMENT ON COLUMN m_product.category IS 'カテゴリ';


-- 在庫マスタ
CREATE TABLE m_stock (
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

-- 在庫マスタのインデックス
CREATE INDEX idx_stock_product ON m_stock(product_id);

-- 在庫マスタのコメント
COMMENT ON TABLE m_stock IS '在庫マスタ';
COMMENT ON COLUMN m_stock.stock_id IS '在庫ID';
COMMENT ON COLUMN m_stock.product_id IS '商品ID';
COMMENT ON COLUMN m_stock.quantity IS '在庫数量';
COMMENT ON COLUMN m_stock.status IS 'ステータス';


-- 注文トランザクション
CREATE TABLE t_order (
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

-- 注文トランザクションのインデックス
CREATE INDEX idx_order_customer ON t_order(customer_id);
CREATE INDEX idx_order_date ON t_order(order_date);

-- 注文トランザクションのコメント
COMMENT ON TABLE t_order IS '注文トランザクション';
COMMENT ON COLUMN t_order.order_id IS '注文ID';
COMMENT ON COLUMN t_order.customer_id IS '顧客ID';
COMMENT ON COLUMN t_order.order_date IS '注文日時';
COMMENT ON COLUMN t_order.status IS 'ステータス';


-- 注文明細トランザクション
CREATE TABLE t_order_item (
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

-- 注文明細トランザクションのインデックス
CREATE INDEX idx_order_item_order ON t_order_item(order_id);

-- 注文明細トランザクションのコメント
COMMENT ON TABLE t_order_item IS '注文明細トランザクション';
COMMENT ON COLUMN t_order_item.order_item_id IS '注文明細ID';
COMMENT ON COLUMN t_order_item.order_id IS '注文ID';
COMMENT ON COLUMN t_order_item.product_id IS '商品ID';
COMMENT ON COLUMN t_order_item.quantity IS '数量';
COMMENT ON COLUMN t_order_item.unit_price IS '単価';
