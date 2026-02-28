
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    current_balance DOUBLE PRECISION NOT NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id),
    CONSTRAINT fk_accounts_user_id_users_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user_id_users_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transactions_account_id_accounts_id FOREIGN KEY (account_id) REFERENCES accounts(id)
);