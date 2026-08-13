package org.example.service;

import org.example.model.TransactionRecord;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LedgerService {
    // Thread-safe map to prevent data concurrency conflicts in memory
    private final ConcurrentHashMap<String, TransactionRecord> activeCache = new ConcurrentHashMap<>();

    // Master Ledger Core Balance
    private double userBalance = 142850300.75;

    public synchronized double getBalance() {
        return this.userBalance;
    }

    public synchronized void deductBalance(double amount) {
        this.userBalance -= amount;
    }

    public void cacheTransaction(TransactionRecord record) {
        activeCache.put(record.getTxId(), record);
    }

    public TransactionRecord getTransaction(String txId) {
        TransactionRecord record = activeCache.get(txId);
        if (record != null && record.isExpired() && !"SETTLED".equals(record.getStatus())) {
            record.setStatus("REJECTED");
            activeCache.remove(txId); // Prune abandoned frames
            return null;
        }
        return record;
    }
}