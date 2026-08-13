package org.example.controller;

import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final AppUserRepository userRepository;

    // In-memory state tracking to hold active transaction tokens and pending balance deductions
    private final Map<String, String> otpStorageCache = new ConcurrentHashMap<>();
    private final Map<String, Double> pendingAmountsCache = new ConcurrentHashMap<>();

    public TransactionController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // STAGE 1: Process Form Submission & Issue One-Time Authorization Code
    @PostMapping("/transactions/initiate")
    public ResponseEntity<?> initiateTransfer(
            @RequestParam String username,
            @RequestParam Double amount) {

        // Find user record inside SQL Server
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Active profile not found in ledger database."));

        // Pre-validation balance check
        if (user.getBalance().doubleValue() < amount) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "reason", "Insufficient ledger funds available to complete remittance."
            ));
        }

        // Generate a random secure 4-digit token matching your frontend PIN layout matrix
        String generatedOtp = String.format("%04d", new Random().nextInt(10000));

        // Cache data using username as key to hold state across HTTP requests
        otpStorageCache.put(username, generatedOtp);
        pendingAmountsCache.put(username, amount);

        // Simulated Out-of-Band Notification Delivery Pipeline
        System.out.println("\n==================================================");
        System.out.println("   APEX BANK SECURE NOTIFICATION DISPATCH WINDOW   ");
        System.out.println("   TARGET DESTINATION: " + user.getEmail());
        System.out.println("   TRANSACTION ALERT CODE: [ " + generatedOtp + " ]");
        System.out.println("==================================================\n");

        return ResponseEntity.ok(Map.of(
                "status", "OTP_SENT",
                "message", "Authorization token dispatched to " + user.getEmail()
        ));
    }

    // STAGE 2: Validate User Pin and Execute SQL Database Balance Deduction
    @PostMapping("/transactions/confirm")
    public ResponseEntity<?> confirmTransfer(
            @RequestParam String username,
            @RequestParam String inputToken) {

        String masterOtp = otpStorageCache.get(username);
        Double amountToDeduct = pendingAmountsCache.get(username);

        if (masterOtp == null || amountToDeduct == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "reason", "No active pending transfer session found."));
        }

        // Validate token equivalence matching the active session cache entry
        if (!masterOtp.equals(inputToken)) {
            return ResponseEntity.status(401).body(Map.of("status", "DENIED", "reason", "Mismatched validation credentials."));
        }

        // Clean up session caches immediately to prevent replay attacks
        otpStorageCache.remove(username);
        pendingAmountsCache.remove(username);

        // Fetch user from DB, perform deduction, and commit back to database storage
        AppUser user = userRepository.findByUsername(username).orElseThrow();
        user.setBalance(user.getBalance().subtract(java.math.BigDecimal.valueOf(amountToDeduct)));
        userRepository.save(user);

        // Generate a unique pipeline trace reference tracking code
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return ResponseEntity.ok(Map.of(
                "status", "APPROVED",
                "txId", transactionId,
                "message", "Remittance complete.",
                "updatedBalance", user.getBalance()
        ));
    }
}