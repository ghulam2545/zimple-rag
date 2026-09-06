CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

drop table if exists document_data;

CREATE TABLE IF NOT EXISTS document_data
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           VARCHAR(128)  NOT NULL,
    workspace         VARCHAR(128)  NOT NULL,
    filename          VARCHAR(1024) NOT NULL,
    file_hash         VARCHAR(64)   NOT NULL,
    file_size         BIGINT        NOT NULL,
    chunks_count      INT           NOT NULL,
    is_public         BOOLEAN          DEFAULT false, -- if true, all users can use
    status            VARCHAR(20)   NOT NULL,
    created_timestamp TIMESTAMPTZ      DEFAULT NOW(),
    UNIQUE (filename, file_hash, user_id, workspace)
);