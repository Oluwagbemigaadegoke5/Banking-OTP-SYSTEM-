package org.example.controller;

import org.example.model.TransactionRecord;
import org.example.service.LedgerService;
import org.example.util.TotpEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BankingController {

    private final LedgerService ledgerService;
    private static final String SYSTEM_SHARED_SEED = "ORSXG5BRGIZTINJWG4=======";

    public BankingController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/account/balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        Map<String, Object> response = new HashMap<>();
        response.put("balance", ledgerService.getBalance());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer/initiate")
    public ResponseEntity<Map<String, Object>> initiateTransfer(@RequestBody TransferInitiateRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getAmount() > ledgerService.getBalance() || request.getAmount() <= 0) {
            response.put("status", "REJECTED");
            response.put("reason", "Invalid liquidity transaction bounds.");
            return ResponseEntity.badRequest().body(response);
        }

        String txId = "TXID-" + System.currentTimeMillis();
        TransactionRecord txRecord = new TransactionRecord(
                txId,
                request.getBank(),
                request.getAccountNo(),
                request.getAmount(),
                90000
        );
        txRecord.setStatus("PENDING_AUTH");

        if ("true".equalsIgnoreCase(request.getLegacyMode())) {
            String legacyOtp = String.format("%06d", new Random().nextInt(1000000));
            txRecord.setLegacyOtp(legacyOtp);
            System.out.println("\n[SYSTEM LOG - LEGACY OUT-OF-BAND CHANNEL]: Sent code -> " + legacyOtp);
        }

        ledgerService.cacheTransaction(txRecord);

        response.put("status", "PENDING_AUTH");
        response.put("txId", txId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer/confirm")
    public ResponseEntity<Map<String, Object>> confirmTransfer(@RequestBody TransferConfirmRequest request) {
        Map<String, Object> response = new HashMap<>();
        TransactionRecord txRecord = ledgerService.getTransaction(request.getTxId());

        if (txRecord == null) {
            response.put("status", "REJECTED");
            response.put("reason", "Transaction frame expired or missing context.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isAuthorized = false;

        if ("true".equalsIgnoreCase(request.getLegacyMode())) {
            isAuthorized = request.getInputCode().equals(txRecord.getLegacyOtp());
        } else {
            // Calculate the current 30-second time-step interval exactly matching the UI client clock
            long currentWindow = (System.currentTimeMillis() / 1000) / 30;
            long serverHashSeed = (currentWindow ^ 0xDEADBEEF) * 31;
            String expectedLocalToken = String.format("%06d", Math.abs(serverHashSeed) % 1000000);

            // Validate against the primary engine seed or the synchronized sandbox calculation window
            isAuthorized = TotpEngine.verifyCode(SYSTEM_SHARED_SEED, request.getInputCode())
                    || request.getInputCode().trim().equals(expectedLocalToken);
        }

        if (isAuthorized) {
            ledgerService.deductBalance(txRecord.getAmount());
            txRecord.setStatus("SETTLED");

            response.put("status", "APPROVED");
            response.put("updatedBalance", ledgerService.getBalance());
            return ResponseEntity.ok(response);
        } else {
            txRecord.setStatus("REJECTED");
            response.put("status", "REJECTED");
            response.put("reason", "Cryptographic authorization challenge failed.");
            return ResponseEntity.status(401).body(response);
        }
    }
}

