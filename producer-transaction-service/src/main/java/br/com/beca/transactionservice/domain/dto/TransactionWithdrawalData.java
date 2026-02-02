package br.com.beca.transactionservice.domain.dto;

import java.math.BigDecimal;

public record TransactionWithdrawalData(
        BigDecimal amount,
        String currency,
        Boolean record
) {
}
