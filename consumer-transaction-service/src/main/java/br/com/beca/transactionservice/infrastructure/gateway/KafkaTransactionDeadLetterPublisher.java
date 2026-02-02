package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionDeadLetterPublisher implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String dlqTopic;

    public KafkaTransactionDeadLetterPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.requested.dlq}") String dlqTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.dlqTopic = dlqTopic;
    }

    @Override
    public void publish(TransactionRequestedEvent event, String reason) {
        kafkaTemplate.send(dlqTopic, event.transactionId().toString(), event);
        kafkaTemplate.send(dlqTopic, event.transactionId().toString(), reason);
    }
}