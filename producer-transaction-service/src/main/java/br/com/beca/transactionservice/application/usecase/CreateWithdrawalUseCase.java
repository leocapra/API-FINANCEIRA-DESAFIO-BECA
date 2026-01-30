package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.TransactionWithdrawalData;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

public record CreateWithdrawalUseCase(TransactionRepository repository, TransactionEventPublisher publisher) {

    public Transaction execute(TransactionWithdrawalData dto) {
        TransactionType type = TransactionType.SAQUE;


        Transaction tx = Transaction.createPending(
                dto.userId(),
                type,
                new Money(dto.amount(), dto.currency()),
                new AccountRef(dto.userId()),
                null,
                null,
                null,
                dto.record(),
                null,
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
