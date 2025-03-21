package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByCustomer(Customer customer);
} 