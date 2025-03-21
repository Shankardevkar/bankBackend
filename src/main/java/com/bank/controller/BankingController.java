package com.bank.controller;

import com.bank.entity.Transaction;
import com.bank.entity.Customer;
import com.bank.entity.User;
import com.bank.service.TransactionService;
import com.bank.service.CustomerService;
import com.bank.service.AccountService;
import com.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/banking")
@CrossOrigin
public class BankingController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/account")
    public ResponseEntity<?> getAccountDetails(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            var account = accountService.getAccountByEmail(email);
            var customer = account.getCustomer();
            
            return ResponseEntity.ok(Map.of(
                "accountNumber", account.getAccountNumber(),
                "accountType", account.getAccountType(),
                "firstName", customer.getFirstName(),
                "lastName", customer.getLastName(),
                "phoneNumber", customer.getPhoneNumber(),
                "address", customer.getAddress()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/account/update")
    public ResponseEntity<?> updateAccount(
            Authentication authentication,
            @RequestBody Map<String, String> updateDetails) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            var account = accountService.getAccountByEmail(email);
            var customer = account.getCustomer();
            
            // Update customer details
            customer.setFirstName(updateDetails.get("firstName"));
            customer.setLastName(updateDetails.get("lastName"));
            customer.setPhoneNumber(updateDetails.get("phoneNumber"));
            customer.setAddress(updateDetails.get("address"));
            
            customerService.updateCustomer(customer.getId(), customer);
            
            return ResponseEntity.ok(Map.of("message", "Account updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            BigDecimal balance = transactionService.getBalance(email);
            return ResponseEntity.ok().body(Map.of("balance", balance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero"));
            }
            
            // Get the authenticated user's email
            String email = authentication.getName();
            
            // Create a transaction with explicit description default value
            String transactionDescription = (description != null && !description.isEmpty()) ? description : "Deposit";
            
            // Call the service method
            Transaction transaction = transactionService.deposit(email, amount, transactionDescription);
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero"));
            }
            
            String email = authentication.getName();
            String transactionDescription = (description != null && !description.isEmpty()) ? description : "Withdrawal";
            Transaction transaction = transactionService.withdraw(email, amount, transactionDescription);
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionHistory(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            List<Transaction> transactions = transactionService.getRecentTransactions(email);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-pin")
    public ResponseEntity<?> checkPin(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(Map.of("pinSet", user.getPin() != null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/set-pin")
    public ResponseEntity<?> setPin(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            String pin = request.get("pin");
            
            if (pin == null || !pin.matches("\\d{6}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid PIN format"));
            }
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setPin(pin);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "PIN set successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            String pin = request.get("pin");
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getPin() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "PIN not set"));
            }
            
            if (!user.getPin().equals(pin)) {
                return ResponseEntity.status(401).body(Map.of("error", "Incorrect PIN"));
            }
            
            return ResponseEntity.ok(Map.of("message", "PIN verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update-pin")
    public ResponseEntity<?> updatePin(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            String email = authentication.getName();
            String pin = request.get("pin");
            
            if (pin == null || !pin.matches("\\d{6}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid PIN format"));
            }
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setPin(pin);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "PIN updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", new Date().toString());
        return ResponseEntity.ok(status);
    }
} 