package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.domain.dto.BankAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class MockApiDeposit implements BankAccountPort {
    private final RestClient client;
    private final String endpoint;
    private final MockApiGetClient getClient;

    public MockApiDeposit(
            @Value("${mockapi.base-url}") String baseUrl,
            @Value("${mockapi.resource}") String endpoint, MockApiGetClient getClient
    ) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.endpoint = endpoint;
        this.getClient = getClient;
    }

    @Override
    public void deposit(String userId, BigDecimal amount) {
        BankAccount clientAccount = getClient.getAccountByUserId(userId);
        var payload = Map.of("balance", clientAccount.balance().add(amount));

        client.put()
                .uri(u -> u
                        .path(endpoint + "/" + clientAccount.id())
                        .build()
                )
                .body(payload)
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
                throw new RuntimeException("Não foi possivel encontrar conta com o userId " + userId);
            }
            return accounts[0];
    }
}
