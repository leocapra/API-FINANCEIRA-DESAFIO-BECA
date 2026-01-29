package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
}
