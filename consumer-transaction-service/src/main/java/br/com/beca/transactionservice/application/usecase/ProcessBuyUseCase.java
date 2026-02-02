package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.BankAccount;
import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionStatus;

import java.math.BigDecimal;

public record ProcessBuyUseCase(TransactionRepository repository, BankAccountPort bankRepository, CurrencyConverterPort converter, TransactionEventPublisher publisher) {
    public void execute(TransactionRequestedEvent event) throws Exception {

        Transaction transaction = repository.findById(event.transactionId()).orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + event.transactionId()));
        BankAccount account = bankRepository.findByUserId(transaction.getUserId().toString());

        if (event.record() != null) {
            if (!event.currency().equals("BRL")) {
                try {
                    BigDecimal newAmount = converter.toBrl(event.amount(), event.currency());
                    BigDecimal fxRate = converter.fxRate(event.currency());
                    transaction.approve();
                    transaction.toBrl(newAmount, fxRate);
                    repository.save(transaction);
                    return;
                } catch (Exception e) {
                    transaction.reject(e.getMessage());
                    repository.save(transaction);
                    publisher.publish(event, e.getMessage());
                    return;
                }
            }
            transaction.approve();
            repository.save(transaction);
            return;
        }

        if (account.id() == null) {
            transaction.reject("Não foi possível encontrar carteira de usuário " + event.uuid());
            repository.save(transaction);
            return;
        }

        if (!hasSufficientBalance(event.amount(), account.balance())) {
            transaction.reject("Saldo insuficiente na carteira!");
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
            try {
                BigDecimal newAmount = converter.toBrl(event.amount(), event.currency());
                BigDecimal fxRate = converter.fxRate(event.currency());
                if (!hasSufficientBalance(newAmount, account.balance())) {
                    transaction.reject("Saldo insuficiente na carteira!");
                    repository.save(transaction);
                    return;
                }
                bankRepository.withdrawal(event.uuid().toString(), newAmount);
                transaction.approve();
                transaction.toBrl(newAmount, fxRate);
                repository.save(transaction);
                return;
            } catch (Exception e) {
                transaction.reject(e.getMessage());
                repository.save(transaction);
                publisher.publish(event, e.getMessage());
                return;
            }
        }

        bankRepository.withdrawal(event.uuid().toString(), event.amount());
        transaction.approve();
        repository.save(transaction);
    }

    private boolean hasSufficientBalance(BigDecimal amount, BigDecimal balance) {
        return balance.compareTo(amount) >= 0;
    }
}
