package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.BankAccount;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;

public record ProcessDepositUseCase(TransactionRepository repository, BankAccountPort bankRepository,
                                    CurrencyConverterPort converter) {
    public void execute(TransactionRequestedEvent event) {

        Transaction transaction = repository.findById(event.transactionId()).orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + event.transactionId()));

        BankAccount account = bankRepository.findByUserId(transaction.getUserId().toString());

        if (event.type() != TransactionType.DEPOSITO) {
            transaction.reject("Tipo de transação " + event.type() + " é diferente de depósito!");
            repository.save(transaction);
            return;
        }


        if (!account.active()) {
            transaction.reject("Conta está desativada!");
            repository.save(transaction);
            return;
        }

        if (!account.currency().equals("BRL")) {
            transaction.reject("Só aceitamos contas em brasileiras, formato " + account.currency() + " inválido!");
            repository.save(transaction);
            return;
        }

        if (transaction.getStatus() == TransactionStatus.APROVADA || transaction.getStatus() == TransactionStatus.REJEITADA) {
            return;
        }

        if (!event.currency().equals("BRL")) {
            BigDecimal newAmount = converter.toBrl(event.amount(), event.currency());
            bankRepository.deposit(event.uuid().toString(), newAmount);
            transaction.approve();
            repository.save(transaction);
        } else {
            bankRepository.deposit(event.uuid().toString(), event.amount());
            transaction.approve();
            repository.save(transaction);
        }


    }
}
