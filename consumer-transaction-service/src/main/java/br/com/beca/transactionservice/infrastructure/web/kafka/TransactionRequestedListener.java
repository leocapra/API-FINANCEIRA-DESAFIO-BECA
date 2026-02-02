package br.com.beca.transactionservice.infrastructure.web.kafka;

import br.com.beca.transactionservice.application.usecase.ControllerUseCase;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionRequestedListener {

    private final ControllerUseCase useCase;

    public TransactionRequestedListener(ControllerUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.requested}")
    @Transactional
    public void onMessage(TransactionRequestedEvent event) throws Exception {
        useCase.execute(event);
    }
}
