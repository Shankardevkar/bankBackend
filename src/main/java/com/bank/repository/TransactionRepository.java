package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop10ByAccountOrderByTransactionDateDesc(Account account);
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
} 