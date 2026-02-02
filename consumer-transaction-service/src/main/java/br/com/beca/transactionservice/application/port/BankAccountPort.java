package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.dto.BankAccount;

import java.math.BigDecimal;

public interface BankAccountPort {
    void deposit(String userId, BigDecimal amount);
    void withdrawal(String userId, BigDecimal amount) throws Exception;
    void transfer (String sourceId, String targetId, BigDecimal amount) throws Exception;
    BankAccount findByUserId(String userId) throws Exception;

}
