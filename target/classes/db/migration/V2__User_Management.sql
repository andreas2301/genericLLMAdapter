-- 1. Create Users Table
CREATE TABLE user_data.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'SUPER_USER')),
    openai_key VARCHAR(255),
    huggingface_key VARCHAR(255),
    deepseek_key VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Index for email lookup
CREATE INDEX idx_users_email ON user_data.users(email);
