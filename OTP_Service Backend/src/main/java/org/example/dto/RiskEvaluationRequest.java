package org.example.dto;

public record RiskEvaluationRequest(
        double amount,
        int isFrequentBeneficiary,
        int hourOfDay,
        double ipRiskScore,
        int consecutiveFailures
) {}