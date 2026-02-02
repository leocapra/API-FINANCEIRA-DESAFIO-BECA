package br.com.beca.userservice.domain.dto;


import java.math.BigDecimal;

public record BankAccount(
        Long id,
        String userId,
        BigDecimal balance,
        String currency,
        boolean active,
        String ownerEmail
) {
}
