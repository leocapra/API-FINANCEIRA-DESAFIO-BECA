package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;
import br.com.beca.transactionservice.infrastructure.persistence.model.TransactionEntity;

import java.util.UUID;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static TransactionEntity toEntity(Transaction tx){

        return new TransactionEntity(
                tx.getId(),
                tx.getUserId(),
                tx.getType(),
                tx.getStatus(),
                tx.getAmount().value(),
                tx.getAmount().currency(),
                UUID.fromString(tx.getSourceAccount().accountId().toString()),
                tx.getTargetAccount(),
                tx.getDescription(),
                tx.getCategory(),
                tx.getCreatedAt(),
                tx.getProcessAt(),
                tx.getCorrelationId(),
                tx.getRejectionReason(),
                tx.getBrl(),
                tx.getFxRate()
        );
    }

    public static Transaction toDomain(TransactionEntity tx){
        return new Transaction(
                tx.getId(),
                tx.getUserId(),
                tx.getType(),
                new Money(tx.getAmount(), tx.getCurrency()),
                new AccountRef(tx.getSourceAccountId()),
                tx.getTargetAccountId(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getCategory(),
                tx.getRejectionReason(),
                tx.getCreatedAt(),
                tx.getProcessedAt(),
                tx.getCorrelationId(),
                tx.getBrl(),
                tx.getFxRate()
        );
    }
}
