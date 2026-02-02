package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.domain.dto.TransactionWithdrawalData;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.exception.FieldIsException;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateWithdrawalUseCase(TransactionRepository repository, TransactionEventPublisher publisher) {

    public Transaction execute(TransactionWithdrawalData dto, TokenInfoData tokenData) {
        List currency = List.of("BRL", "EUR", "USD", "AUD", "CAD", "NZD", "CHF", "GBP", "JPY", "MXN");
        if (tokenData.userId() == null) throw new FieldIsException("UserId não pode ser nulo!");
        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) throw new FieldIsException("Quantidade deve ser maior que zero!");
        if (dto.currency() == null) throw new FieldIsException("Moeda não pode ser null!");
        if (dto.currency().toString().length() > 3) throw new FieldIsException("Moeda não é valida!");
        switch (dto.currency()) {
            case "BRL", "EUR", "USD", "AUD", "CAD", "NZD", "CHF", "GBP", "JPY", "MXN" -> {
            }
            default -> throw new FieldIsException("Moeda inválida: " + dto.currency() + " tente " + currency);
        }

        TransactionType type = TransactionType.SAQUE;

        Transaction tx = Transaction.createPending(
                UUID.fromString(tokenData.userId()),
                type,
                new Money(dto.amount(), dto.currency()),
                new AccountRef(UUID.fromString(tokenData.userId())),
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
