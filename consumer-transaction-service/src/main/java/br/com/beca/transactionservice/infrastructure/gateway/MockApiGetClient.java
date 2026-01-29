package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.domain.dto.BankAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MockApiGetClient {
    private final RestClient client;
    private final String endpoint;

    public MockApiGetClient(
            @Value("${mockapi.base-url}") String baseUrl,
            @Value("${mockapi.resource}") String endpoint
    ) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.endpoint = endpoint;
    }

    public BankAccount getAccountByUserId(String userId) {
        BankAccount[] accounts = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .body(BankAccount[].class);

        if (accounts == null || accounts.length == 0) {
            throw new RuntimeException("Não foi possivel encontrar conta com o userId " + userId);
        }
        return accounts[0];
    }
}
