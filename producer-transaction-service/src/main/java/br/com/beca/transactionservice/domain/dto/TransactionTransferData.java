package br.com.beca.transactionservice.domain.dto;

import br.com.beca.transactionservice.domain.model.BuyType;
import br.com.beca.transactionservice.domain.model.TransferType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionTransferData(
        BigDecimal amount,
        String currency,
        UUID targetAccountId,
        TransferType transferType,
        Boolean record
) {
}
