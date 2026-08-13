package org.example.model;

public class TransactionRecord {
    private String txId;
    private String bank;
    private String accountNo;
    private double amount;
    private String status; // INITIATED, PENDING_AUTH, SETTLED, REJECTED
    private String legacyOtp; // Nullable, only populated during control group checks
    private long expiryTimestamp;

    public TransactionRecord(String txId, String bank, String accountNo, double amount, long expiryWindowMillis) {
        this.txId = txId;
        this.bank = bank;
        this.accountNo = accountNo;
        this.amount = amount;
        this.status = "INITIATED";
        this.expiryTimestamp = System.currentTimeMillis() + expiryWindowMillis;
    }

    // Getters and Setters
    public String getTxId() { return txId; }
    public String getBank() { return bank; }
    public String getAccountNo() { return accountNo; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLegacyOtp() { return legacyOtp; }
    public void setLegacyOtp(String legacyOtp) { this.legacyOtp = legacyOtp; }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expiryTimestamp;
    }
}