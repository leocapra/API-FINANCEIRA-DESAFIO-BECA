package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository jpa;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction transaction) {
        jpa.save(TransactionMapper.toEntity(transaction));
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id).map(TransactionMapper::toDomain);
    }
}
