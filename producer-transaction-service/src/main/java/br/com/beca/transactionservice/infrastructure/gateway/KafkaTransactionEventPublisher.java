package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String requestTopic;

    public KafkaTransactionEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.requested}") String requestTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.requestTopic = requestTopic;
    }

    @Override
    public void publish(TransactionRequestedEvent event) {
        kafkaTemplate.send(requestTopic, event.transactionId().toString(), event);
    }
}
