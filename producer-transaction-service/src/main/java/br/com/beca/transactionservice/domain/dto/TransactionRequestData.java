package br.com.beca.transactionservice.domain.dto;

import br.com.beca.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequestData(
        UUID userId,
        BigDecimal amount,
        String currency,
        String sourceAccountId,
        String targetAccountId,
        String description,
        String category
) {
}
