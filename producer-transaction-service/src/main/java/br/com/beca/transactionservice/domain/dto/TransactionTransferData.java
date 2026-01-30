package br.com.beca.transactionservice.domain.dto;

import br.com.beca.transactionservice.domain.model.BuyType;
import br.com.beca.transactionservice.domain.model.TransferType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionTransferData(
        UUID userId,
        BigDecimal amount,
        String currency,
        String targetAccountId,
        TransferType transferType,
        Boolean record
) {
}
