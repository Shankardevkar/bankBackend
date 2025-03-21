-- Create the database
CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(10) NOT NULL,
    address TEXT NOT NULL,
    user_id BIGINT,
    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT fk_customers_users FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    created_at DATETIME NOT NULL,
    customer_id BIGINT,
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customers FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    description TEXT,
    transaction_date DATETIME NOT NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date); 