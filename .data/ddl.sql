
CREATE TABLE wallets (
		id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(50) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0 ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	currency VARCHAR(6) NOT NULL
);



CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    related_wallet_id UUID DEFAULT NULL,
    sequence_no BIGSERIAL, 
	currency VARCHAR(6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	balance_after DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    FOREIGN KEY (related_wallet_id) REFERENCES wallets(id)
);


CREATE USER wallet_system_user WITH PASSWORD '123456';

ALTER TABLE wallets OWNER TO wallet_system_user;
ALTER TABLE transactions OWNER TO wallet_system_user;

