CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  client_id UUID NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  category VARCHAR(50) NOT NULL,
  source VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE bonus_ledger (
  client_id UUID NOT NULL,
  transaction_id UUID NOT NULL,
  points DECIMAL(15,2) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE bonus_rules (
  id VARCHAR(50) PRIMARY KEY,
  condition JSONB NOT NULL,
  reward_type VARCHAR(20) NOT NULL,
  reward_value DECIMAL(5,2) NOT NULL,
  priority INTEGER NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true
);