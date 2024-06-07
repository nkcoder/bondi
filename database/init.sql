CREATE TABLE IF NOT EXISTS member
(
    id                  VARCHAR(50) PRIMARY KEY,
    mobile_number       VARCHAR(512)        NOT NULL,
    email               VARCHAR(512) UNIQUE NOT NULL,
    outstanding_balance DECIMAL(14, 5) DEFAULT 0.00,
    dd_failure_count    INT            DEFAULT 0
);