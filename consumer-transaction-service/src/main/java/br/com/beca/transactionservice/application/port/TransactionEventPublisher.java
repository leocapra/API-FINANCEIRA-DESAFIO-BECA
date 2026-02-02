package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;

public interface TransactionEventPublisher {
    void publish(TransactionRequestedEvent event, String reason);
}

