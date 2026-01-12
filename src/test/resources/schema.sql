-- H2データベース用テーブル作成SQL（テスト用）
-- PostgreSQL互換モード

-- ユーザーマスタ
CREATE TABLE IF NOT EXISTS m_user (
    user_id         VARCHAR(36) PRIMARY KEY,
    user_name       VARCHAR(50) NOT NULL,
    password        VARCHAR(100) NOT NULL,
    role            SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(36),
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(36),
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_user_name UNIQUE (user_name)
);

-- ユーザーマスタのインデックス
CREATE INDEX IF NOT EXISTS idx_user_name ON m_user(user_name);
CREATE INDEX IF NOT EXISTS idx_user_role ON m_user(role);

-- 顧客マスタ
CREATE TABLE IF NOT EXISTS m_customer (
    customer_id     VARCHAR(36) PRIMARY KEY,
    customer_number VARCHAR(10) NOT NULL,
    customer_name   VARCHAR(50) NOT NULL,
    address         VARCHAR(100),
    phone_number    VARCHAR(20),
    email           VARCHAR(50),
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(36),
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(36),
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_customer_number UNIQUE (customer_number)
);

-- 顧客マスタのインデックス
CREATE INDEX IF NOT EXISTS idx_customer_name ON m_customer(customer_name);
CREATE INDEX IF NOT EXISTS idx_customer_address ON m_customer(address);
CREATE INDEX IF NOT EXISTS idx_customer_phone ON m_customer(phone_number);
CREATE INDEX IF NOT EXISTS idx_customer_email ON m_customer(email);

-- 商品マスタ
CREATE TABLE IF NOT EXISTS m_product (
    product_id      VARCHAR(36) PRIMARY KEY,
    product_number  VARCHAR(10) NOT NULL,
    product_name    VARCHAR(50) NOT NULL,
    description     VARCHAR(100),
    price           INT NOT NULL,
    category        SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(36),
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(36),
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_product_number UNIQUE (product_number)
);

-- 商品マスタのインデックス
CREATE INDEX IF NOT EXISTS idx_product_name ON m_product(product_name);
CREATE INDEX IF NOT EXISTS idx_product_price ON m_product(price);

-- 在庫マスタ
CREATE TABLE IF NOT EXISTS m_stock (
    stock_id        VARCHAR(36) PRIMARY KEY,
    product_id      VARCHAR(36) NOT NULL,
    quantity        INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(36),
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(36),
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_stock_product UNIQUE (product_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES m_product(product_id)
);

-- 在庫マスタのインデックス
CREATE INDEX IF NOT EXISTS idx_stock_product ON m_stock(product_id);

-- 注文トランザクション
CREATE TABLE IF NOT EXISTS t_order (
    order_id        VARCHAR(36) PRIMARY KEY,
    customer_id     VARCHAR(36) NOT NULL,
    order_date      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          SMALLINT NOT NULL,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(36),
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(36),
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES m_customer(customer_id)
);

-- 注文トランザクションのインデックス
CREATE INDEX IF NOT EXISTS idx_order_customer ON t_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_date ON t_order(order_date);

-- 注文明細トランザクション
CREATE TABLE IF NOT EXISTS t_order_item (
    order_item_id   VARCHAR(36) PRIMARY KEY,
    order_id        VARCHAR(36) NOT NULL,
    product_id      VARCHAR(36) NOT NULL,
    quantity        INT NOT NULL,
    unit_price      INT NOT NULL,
    create_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES t_order(order_id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES m_product(product_id)
);

-- 注文明細トランザクションのインデックス
CREATE INDEX IF NOT EXISTS idx_order_item_order ON t_order_item(order_id);
