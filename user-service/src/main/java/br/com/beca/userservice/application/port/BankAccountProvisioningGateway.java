package br.com.beca.userservice.application.port;

public interface BankAccountProvisioningGateway {
    void createAccountForUser(String userId, String email);
}
