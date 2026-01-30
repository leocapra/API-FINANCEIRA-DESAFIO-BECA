package br.com.beca.transactionservice.domain.dto;

import br.com.beca.transactionservice.domain.model.BuyType;
import br.com.beca.transactionservice.domain.model.TransferType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionBuyData(
        UUID userId,
        BigDecimal amount,
        String currency,
        String description,
        String category,
        BuyType buyType,
        Boolean record
        ) {
}
