package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.application.port.BankAccountProvisioningGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class MockApiBankAccountProvisioningGateway implements BankAccountProvisioningGateway {
    private final RestClient client;
    private final String endpoint;

    public MockApiBankAccountProvisioningGateway(
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
}
