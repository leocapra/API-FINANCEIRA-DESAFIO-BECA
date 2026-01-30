CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,

    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transfer_type VARCHAR(50),
    buy_type VARCHAR(50),

    amount NUMERIC(19, 4) NOT NULL,
    brl NUMERIC(19, 4),
    fx_rate NUMERIC(19, 8),

    source_account_id UUID NOT NULL,
    target_account_id UUID,

    currency VARCHAR(10) NOT NULL,

    description TEXT,
    category VARCHAR(100),
    rejection_reason TEXT,

    record BOOLEAN DEFAULT FALSE,

    correlation_id VARCHAR(100) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_correlation_id ON transactions (correlation_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);