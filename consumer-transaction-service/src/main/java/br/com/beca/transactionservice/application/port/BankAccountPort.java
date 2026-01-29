package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.dto.BankAccount;

import java.math.BigDecimal;

public interface BankAccountPort {
    void deposit(String userId, BigDecimal amount);
    void withdrawal(String userId, BigDecimal amount);
    BankAccount findByUserId(String userId);

}
