package br.com.beca.transactionservice.domain.event;

import br.com.beca.transactionservice.domain.model.BuyType;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.model.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        LocalDateTime createdAt,
        String correlationId,
        Boolean record,
        TransferType transferType,
        BuyType buyType
) {
}
