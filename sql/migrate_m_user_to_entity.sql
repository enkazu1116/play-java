-- m_user を Entity (MUser.java) に合わせて再作成する
-- 既存テーブルを削除してから、create_tables.sql と同じ定義で作成する

DROP TABLE IF EXISTS m_user CASCADE;

-- user_id は Entity の String (ASSIGN_UUID) に合わせて VARCHAR(36)
CREATE TABLE m_user (
    user_id         VARCHAR(36) PRIMARY KEY,
    user_name       VARCHAR(50) NOT NULL,
    password        VARCHAR(100) NOT NULL,
    role            INTEGER,
    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    create_user     VARCHAR(50),
    create_date     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user     VARCHAR(50),
    update_date     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_name UNIQUE (user_name)
);

CREATE INDEX idx_user_name ON m_user(user_name);
CREATE INDEX idx_user_role ON m_user(role);

COMMENT ON TABLE m_user IS 'ユーザーマスタ';
COMMENT ON COLUMN m_user.user_id IS 'ユーザーID';
COMMENT ON COLUMN m_user.user_name IS 'ユーザー名';
COMMENT ON COLUMN m_user.password IS 'パスワード';
COMMENT ON COLUMN m_user.role IS 'ロール';
COMMENT ON COLUMN m_user.delete_flag IS '削除フラグ';
