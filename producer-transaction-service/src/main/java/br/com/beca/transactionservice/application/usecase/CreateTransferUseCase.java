package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.TransactionRequestData;
import br.com.beca.transactionservice.domain.dto.TransactionTransferData;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.model.TransferType;
import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

import java.util.UUID;

public record CreateTransferUseCase(TransactionRepository repository, TransactionEventPublisher publisher) {

    public Transaction execute(TransactionTransferData dto) {
        TransactionType type = TransactionType.TRANSFERENCIA;

        Transaction tx = Transaction.createPending(
                dto.userId(),
                type,
                new Money(dto.amount(), dto.currency()),
                new AccountRef(dto.userId()),
                UUID.fromString(dto.targetAccountId()),
                null,
                null,
                dto.record(),
                dto.transferType(),
                null
        );

        Transaction saved = repository.save(tx);

        TransactionRequestedEvent event = new TransactionRequestedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getType(),
                saved.getAmount().value(),
                saved.getAmount().currency(),
                saved.getSourceAccount().accountId(),
                saved.getTargetAccount(),
                saved.getDescription(),
                saved.getCategory(),
                saved.getCreatedAt(),
                saved.getCorrelationId(),
                saved.getRecord(),
                saved.getTransferType(),
                saved.getBuyType()
        );
        publisher.publish(event);

        return saved;
    }

}
