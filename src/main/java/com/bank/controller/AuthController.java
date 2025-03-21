package com.bank.controller;

import com.bank.entity.Customer;
import com.bank.entity.User;
import com.bank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phoneNumber,
            @RequestParam String address,
            RedirectAttributes redirectAttributes) {
        try {
            validateRegistrationInput(email, password, firstName, lastName, phoneNumber, address);
            
            User user = createUser(email, password);
            Customer customer = createCustomer(email, firstName, lastName, phoneNumber, address);
            
            authService.registerUser(user, customer);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login.html";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/register.html";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login.html";
        }
        return "forward:/dashboard.html";
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login.html?logout=true";
    }

    private void validateRegistrationInput(String email, String password, String firstName, 
            String lastName, String phoneNumber, String address) {
        if (email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6 ||
            firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            phoneNumber == null || !phoneNumber.matches("[0-9]{10}") ||
            address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        return user;
    }

    private Customer createCustomer(String email, String firstName, String lastName, 
            String phoneNumber, String address) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        return customer;
    }
} 