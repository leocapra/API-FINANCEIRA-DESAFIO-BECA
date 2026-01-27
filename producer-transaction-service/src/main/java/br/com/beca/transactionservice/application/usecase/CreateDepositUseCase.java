package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.TransactionRequestData;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

public record CreateDepositUseCase(TransactionRepository repository) {


    public Transaction execute(TransactionRequestData dto) {
        TransactionType type = TransactionType.DEPOSITO;


        Transaction tx = Transaction.createPending(
                dto.userId(),
                type,
                new Money(dto.amount(), dto.currency()),
                new AccountRef(dto.userId()),
                null,
                dto.description(),
                dto.category()
        );

        return repository.save(tx);
    }
}
