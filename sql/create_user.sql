
CREATE TABLE m_user (
    user_id         BIGSERIAL PRIMARY KEY,
    login_id        VARCHAR(50) NOT NULL,
    user_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(30) NOT NULL DEFAULT 'user',
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    last_login_at   TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT
);