package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> search(
                UUID userId,
                TransactionStatus status,
                TransactionType type,
                LocalDateTime startCreatedAt,
                LocalDateTime endCreatedAt
        );

}
