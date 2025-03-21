# Bank Management System

A Spring Boot-based REST API for managing bank customers, accounts, and transactions.

## Features

- Customer Management

  - Create, read, update, and delete customers
  - Customer information includes name, email, phone number, and address

- Account Management

  - Create new accounts for customers
  - View account details and balance
  - Update account information
  - Delete accounts
  - Deposit and withdraw funds

- Transaction Management
  - Create transactions (deposits and withdrawals)
  - View transaction history
  - Transfer funds between accounts
  - Delete transactions

## Technologies Used

- Spring Boot 3.2.3
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### Customers

- POST `/api/customers` - Create a new customer
- GET `/api/customers/{id}` - Get customer by ID
- GET `/api/customers` - Get all customers
- PUT `/api/customers/{id}` - Update customer
- DELETE `/api/customers/{id}` - Delete customer

### Accounts

- POST `/api/accounts/customer/{customerId}` - Create a new account
- GET `/api/accounts/{id}` - Get account by ID
- GET `/api/accounts/customer/{customerId}` - Get all accounts for a customer
- PUT `/api/accounts/{id}` - Update account
- DELETE `/api/accounts/{id}` - Delete account
- POST `/api/accounts/{id}/deposit` - Deposit money
- POST `/api/accounts/{id}/withdraw` - Withdraw money

### Transactions

- POST `/api/transactions/account/{accountId}` - Create a new transaction
- GET `/api/transactions/{id}` - Get transaction by ID
- GET `/api/transactions/account/{accountId}` - Get all transactions for an account
- DELETE `/api/transactions/{id}` - Delete transaction
- POST `/api/transactions/transfer` - Transfer money between accounts

## H2 Console

The H2 database console is available at `http://localhost:8080/h2-console` when the application is running.

## Sample Requests

### Create a Customer

```json
POST /api/customers
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "1234567890",
    "address": "123 Main St"
}
```

### Create an Account

```json
POST /api/accounts/customer/1
{
    "accountNumber": "ACC001",
    "accountType": "SAVINGS"
}
```

### Create a Transaction

```json
POST /api/transactions/account/1
{
    "transactionType": "DEPOSIT",
    "amount": 1000.00,
    "description": "Initial deposit"
}
```

### Transfer Money

```
POST /api/transactions/transfer?fromAccountId=1&toAccountId=2&amount=500.00
```
