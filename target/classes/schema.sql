-- Drop existing tables in reverse order of dependencies
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    pin VARCHAR(6),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Create customers table
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT fk_customers_users FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create accounts table
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_id BIGINT NOT NULL,
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customers FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL DEFAULT 'CREDIT',
    amount DECIMAL(19,4) NOT NULL,
    description TEXT,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id BIGINT NOT NULL,
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_user_id ON customers(user_id);
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date); 