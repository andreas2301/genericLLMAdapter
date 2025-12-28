-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- 1. Defining Data Schema (Tenants, Configs)
CREATE SCHEMA IF NOT EXISTS defining_data;

CREATE TABLE defining_data.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE defining_data.system_prompts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID REFERENCES defining_data.tenants(id),
    name VARCHAR(100) NOT NULL,
    prompt_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Operational Data Schema (Sessions, Vectors)
CREATE SCHEMA IF NOT EXISTS operational_data;

CREATE TABLE operational_data.sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- tenant_id UUID REFERENCES defining_data.tenants(id), 
    user_id UUID REFERENCES defining_data.users(id),
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_interaction_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE TABLE operational_data.context_vectors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES operational_data.sessions(id),
    content TEXT,
    embedding vector(1536), -- Assuming OpenAI 1536 dims, customizable
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for vector search (Cosine Similarity)
CREATE INDEX ON operational_data.context_vectors USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);


-- 3. Log Data Schema (Interactions, Compliance)
CREATE SCHEMA IF NOT EXISTS log_data;

CREATE TABLE log_data.interaction_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES operational_data.sessions(id),
    role VARCHAR(50) NOT NULL, -- USER, ASSISTANT, SYSTEM
    content TEXT NOT NULL,
    token_count INT,
    provider VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Basic RLS (Stub - setup requirements for actual RLS usually involves connection-level settings)
ALTER TABLE operational_data.sessions ENABLE ROW LEVEL SECURITY;
-- Policy example (commented out until tenant context propagation is implemented)
-- CREATE POLICY tenant_isolation ON operational_data.sessions
--     USING (tenant_id = current_setting('app.current_tenant')::uuid);
