CREATE TABLE IF NOT EXISTS chat_threads (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    last_ticker VARCHAR(16) NULL,
    last_year INT NULL,
    context_summary LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_chat_threads_user_updated ON chat_threads (user_id, updated_at);

CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    thread_id VARCHAR(36) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content LONGTEXT NOT NULL,
    tool_call_id VARCHAR(255) NULL,
    provider VARCHAR(32) NULL,
    model VARCHAR(64) NULL,
    input_tokens INT NULL,
    output_tokens INT NULL,
    total_tokens INT NULL,
    cost_usd DECIMAL(18, 8) NULL,
    latency_ms INT NULL,
    tool_calls_json LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_chat_messages_thread FOREIGN KEY (thread_id) REFERENCES chat_threads(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_thread_created ON chat_messages (thread_id, created_at);
CREATE INDEX idx_chat_messages_thread_role ON chat_messages (thread_id, role);

CREATE TABLE IF NOT EXISTS chat_message_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(36) NOT NULL,
    chunk_id VARCHAR(100) NULL,
    source_title VARCHAR(255) NULL,
    page_number INT NULL,
    score DECIMAL(12, 6) NULL,
    CONSTRAINT fk_chat_message_sources_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_message_sources_message ON chat_message_sources (message_id);
