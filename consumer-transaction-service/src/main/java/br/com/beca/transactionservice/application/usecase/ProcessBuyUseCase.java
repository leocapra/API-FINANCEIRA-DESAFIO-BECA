package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.BankAccount;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionStatus;

import java.math.BigDecimal;

public record ProcessBuyUseCase(TransactionRepository repository, BankAccountPort bankRepository, CurrencyConverterPort converter) {
    public void execute(TransactionRequestedEvent event) {

        Transaction transaction = repository.findById(event.transactionId()).orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + event.transactionId()));
        BankAccount account = bankRepository.findByUserId(transaction.getUserId().toString());

        if (event.record() != null) {
            if (!event.currency().equals("BRL")) {
                BigDecimal newAmount = converter.toBrl(event.amount(), event.currency());
                BigDecimal fxRate = converter.fxRate(event.currency());
                transaction.approve();
                transaction.toBrl(newAmount, fxRate);
                repository.save(transaction);
                return;
            }
            transaction.approve();
            repository.save(transaction);
            return;
        }

        if (!account.active()) {
            transaction.reject("Conta está desativada!");
            repository.save(transaction);
            return;
        }

        if (!account.currency().equals("BRL")) {
            transaction.reject("Só aceitamos contas brasileiras, formato " + account.currency() + " inválido!");
            repository.save(transaction);
            return;
        }

        if (transaction.getStatus() == TransactionStatus.APROVADA || transaction.getStatus() == TransactionStatus.REJEITADA) {
            return;
        }

        if (!event.currency().equals("BRL")) {
            BigDecimal newAmount = converter.toBrl(event.amount(), event.currency());
            BigDecimal fxRate = converter.fxRate(event.currency());
            if (newAmount.compareTo(account.balance()) > 0){
                transaction.reject("Saldo indisponível!");
                repository.save(transaction);
                return;
            }
            bankRepository.withdrawal(event.uuid().toString(), newAmount);
            transaction.approve();
            transaction.toBrl(newAmount, fxRate);
            repository.save(transaction);
            return;
        }

        if (event.amount().compareTo(account.balance()) > 0){
            transaction.reject("Saldo indisponível!");
            repository.save(transaction);
            return;
        }

        bankRepository.withdrawal(event.uuid().toString(), event.amount());
        transaction.approve();
        repository.save(transaction);


    }
}
