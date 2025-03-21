package com.bank.controller;

import com.bank.entity.Transaction;
import com.bank.service.TransactionService;
import com.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    // Deposit money
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Transaction transaction = transactionService.deposit(email, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Withdraw money
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Transaction transaction = transactionService.withdraw(email, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Check balance
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        try {
            String email = authentication.getName();
            BigDecimal balance = accountService.getBalance(email);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get transaction history
    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Transaction> transactions = transactionService.getRecentTransactions(email);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/account/{accountId}")
    public ResponseEntity<?> createTransaction(
            @PathVariable Long accountId,
            @Valid @RequestBody Map<String, Object> request) {
        try {
            if (!request.containsKey("type") || !request.containsKey("amount")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Type and amount are required fields"));
            }

            String type = ((String) request.get("type")).toUpperCase();
            if (!type.equals("CREDIT") && !type.equals("DEBIT")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Transaction type must be either CREDIT or DEBIT"));
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(request.get("amount").toString());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount format"));
            }

            String description = request.containsKey("description") ? (String) request.get("description") : null;
            
            // Get the account
            var account = accountService.getAccountById(accountId);
            
            // Create the transaction
            Transaction transaction = transactionService.createTransaction(account, type, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(transactionService.transfer(fromAccountId, toAccountId, amount));
    }
} 