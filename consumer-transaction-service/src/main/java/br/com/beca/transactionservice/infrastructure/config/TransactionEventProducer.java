package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicRequested;

    public TransactionEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.requested.dlq}") String topicRequested
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicRequested = topicRequested;
    }

    public void publishRequested(TransactionRequestedEvent event){
        kafkaTemplate.send(topicRequested, event.transactionId().toString(), event);
    }
}
