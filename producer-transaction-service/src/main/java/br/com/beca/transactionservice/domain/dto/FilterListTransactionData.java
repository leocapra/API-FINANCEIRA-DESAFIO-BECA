package br.com.beca.transactionservice.domain.dto;

import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record FilterListTransactionData(
        UUID userId,
        TransactionStatus status,
        TransactionType type,
        LocalDateTime startCreatedAt,
        LocalDateTime endCreatedAt
) { }
