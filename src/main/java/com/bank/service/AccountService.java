package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Account createAccount(Long customerId, Account account) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        if (accountRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }

        account.setCustomer(customer);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with account number: " + accountNumber));
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Account updateAccount(Long id, Account accountDetails) {
        Account account = getAccountById(id);
        
        account.setAccountType(accountDetails.getAccountType());
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = getAccountById(accountId);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = getAccountById(accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    public BigDecimal getBalance(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Account account = accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return account.getBalance();
    }

    @Transactional
    public Account createAccount(User user, String accountType) {
        if (user.getCustomer() == null) {
            throw new RuntimeException("User has no associated customer");
        }

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(user.getCustomer());
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        // Simple implementation - in production, use a more sophisticated method
        return String.format("%010d", System.currentTimeMillis() % 10000000000L);
    }

    public Account getAccountByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return accountRepository.findByCustomer(user.getCustomer())
            .orElseThrow(() -> new RuntimeException("Account not found"));
    }
} 