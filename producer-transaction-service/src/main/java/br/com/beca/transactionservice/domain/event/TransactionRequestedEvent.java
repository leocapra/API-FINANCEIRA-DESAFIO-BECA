package br.com.beca.transactionservice.domain.event;

import br.com.beca.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRequestedEvent(
        UUID transactionId,
        UUID uuid,
        TransactionType type,
        BigDecimal amount,
        String currency,
        UUID sourceAccountId,
        UUID targetAccountId,
        String description,
        String categoty,
        Instant createdAt,
        String correlationId,
        boolean record
) {
}
