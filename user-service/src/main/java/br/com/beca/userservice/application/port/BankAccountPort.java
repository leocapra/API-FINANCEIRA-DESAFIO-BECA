package br.com.beca.userservice.application.port;

import br.com.beca.userservice.domain.dto.BankAccount;

public interface BankAccountPort {
    void createAccountForUser(String userId, String email);
    void deleteAccountById(String userId);
    BankAccount findByUserId(String userId);
    void updateAccountEmail(String userId, String email);
}
