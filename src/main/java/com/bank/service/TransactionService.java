package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    public BigDecimal getBalance(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return account.getBalance();
    }

    @Transactional
    public Transaction deposit(String email, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));

        // Create new transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "Deposit");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType("CREDIT");

        // Update account balance
        account.setBalance(account.getBalance().add(amount));
        
        // First save the account to ensure balance is updated
        accountRepository.save(account);
        
        // Then save and return the transaction
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String email, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "Withdrawal");
        transaction.setTransactionDate(LocalDateTime.now());

        // Update account balance
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getRecentTransactions(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return transactionRepository.findTop10ByAccountOrderByTransactionDateDesc(account);
    }

    @Transactional
    public Transaction createTransaction(Account account, String type, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (!type.equals("CREDIT") && !type.equals("DEBIT")) {
            throw new IllegalArgumentException("Transaction type must be either CREDIT or DEBIT");
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : (type.equals("CREDIT") ? "Deposit" : "Withdrawal"));
        transaction.setTransactionDate(LocalDateTime.now());

        // Update account balance
        if ("CREDIT".equals(type)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("DEBIT".equals(type)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
        return transactionRepository.findByAccountOrderByTransactionDateDesc(account);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        // Revert the account balance
        Account account = transaction.getAccount();
        if ("CREDIT".equals(transaction.getTransactionType())) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else if ("DEBIT".equals(transaction.getTransactionType())) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }
        
        accountRepository.save(account);
        transactionRepository.delete(transaction);
    }

    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new EntityNotFoundException("From account not found with id: " + fromAccountId));
        
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new EntityNotFoundException("To account not found with id: " + toAccountId));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source account");
        }

        // Create withdrawal transaction
        Transaction withdrawal = createTransaction(fromAccount, "DEBIT", amount, 
            "Transfer to account: " + toAccount.getAccountNumber());

        // Create deposit transaction
        Transaction deposit = createTransaction(toAccount, "CREDIT", amount, 
            "Transfer from account: " + fromAccount.getAccountNumber());

        return deposit;
    }
} 