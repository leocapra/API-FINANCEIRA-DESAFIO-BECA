package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;

public record ProcessDepositUseCase(TransactionRepository repo) {
    public void execute(TransactionRequestedEvent event) {
        var tx = repo.findById(event.transactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + event.transactionId()));

        if (tx.getStatus() == TransactionStatus.APROVADA || tx.getStatus() == TransactionStatus.REJEITADA) {
            return;
        }

        if (event.type() == TransactionType.DEPOSITO) {
            tx.approve();
            repo.save(tx);
            return;
        }

        tx.reject("Tipo não implementado no MS3 ainda");
        repo.save(tx);
    }
}
