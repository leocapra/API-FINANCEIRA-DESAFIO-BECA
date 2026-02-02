package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.domain.dto.BankAccount;
import br.com.beca.userservice.domain.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class MockApiAdapter implements BankAccountPort {
    private final RestClient client;
    private final String endpoint;

    public MockApiAdapter(
            @Value("${mockapi.base-url}") String baseUrl,
            @Value("${mockapi.resource}") String endpoint
            ) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.endpoint = endpoint;
    }

    @Override
    public void createAccountForUser(String userId, String email) {
        var payload = Map.of(
                "userId", userId,
                "balance", 0,
                "currency", "BRL",
                "active", true,
                "ownerEmail", email
        );

        client.post()
                .uri(endpoint)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void updateAccountEmail(String userId, String email) {
        BankAccount bankAccount = this.findByUserId(userId);

        var payload = Map.of(
                "ownerEmail", email
        );

        client.put()
                .uri(endpoint + "/{id}", bankAccount.id())
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteAccountById(String userId) {
        BankAccount bankAccount = this.findByUserId(userId);
        if (bankAccount == null) {
            throw new NotFoundException("Não foi possivel encontrar conta com o userId " + userId);
        }
        client.delete()
                .uri(endpoint + "/{id}", bankAccount.id())
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public BankAccount findByUserId(String userId) {
        BankAccount[] accounts = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .body(BankAccount[].class);

        if (accounts == null || accounts.length == 0) {
            throw new NotFoundException("Não foi possivel encontrar conta com o userId " + userId);
        }
        return accounts[0];
    }

}
