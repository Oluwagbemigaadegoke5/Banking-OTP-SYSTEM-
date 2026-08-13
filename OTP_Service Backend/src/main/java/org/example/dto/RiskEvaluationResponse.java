package org.example.dto;

public record RiskEvaluationResponse(
        double riskScore,
        boolean isAnomaly,
        String recommendation
) {}