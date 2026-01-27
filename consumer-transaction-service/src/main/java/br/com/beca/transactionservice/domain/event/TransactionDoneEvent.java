package br.com.beca.transactionservice.domain.event;

import br.com.beca.transactionservice.domain.model.TransactionStatus;

import java.time.Instant;
import java.util.UUID;

public record TransactionDoneEvent(
        UUID transactionId,
        TransactionStatus status,
        String reason,
        Instant processedAt,
        String correlationId
) {
}
