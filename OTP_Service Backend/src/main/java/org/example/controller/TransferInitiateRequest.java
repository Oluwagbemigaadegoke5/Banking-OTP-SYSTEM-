package org.example.controller;

// Data structure representing the inbound initiation payload context
class TransferInitiateRequest {
    private String bank;
    private String accountNo;
    private double amount;
    private String legacyMode;

    // Getters and Setters
    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getLegacyMode() { return legacyMode; }
    public void setLegacyMode(String legacyMode) { this.legacyMode = legacyMode; }
}

// Data structure representing the confirmation token block
class TransferConfirmRequest {
    private String txId;
    private String inputCode;
    private String legacyMode;

    // Getters and Setters
    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }
    public String getInputCode() { return inputCode; }
    public void setInputCode(String inputCode) { this.inputCode = inputCode; }
    public String getLegacyMode() { return legacyMode; }
    public void setLegacyMode(String legacyMode) { this.legacyMode = legacyMode; }
}