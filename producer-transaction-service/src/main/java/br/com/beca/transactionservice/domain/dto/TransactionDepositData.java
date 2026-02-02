package br.com.beca.transactionservice.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDepositData(
        BigDecimal amount,
        String currency,
        Boolean record
) {
}
