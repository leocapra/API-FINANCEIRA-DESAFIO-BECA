package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.infrastructure.persistence.model.TransactionEntity;
import br.com.beca.transactionservice.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository jpa;
    private final TransactionMapper mapper;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa, TransactionMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        jpa.save(TransactionMapper.toEntity(transaction));
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Transaction> search(
            UUID userId,
            TransactionStatus status,
            TransactionType type,
            LocalDateTime startCreatedAt,
            LocalDateTime endCreatedAt
    ) {
        List<TransactionEntity> transcationsEntity = jpa.search(
                userId,
                status,
                type,
                startCreatedAt,
                endCreatedAt
                );
        return transcationsEntity.stream().map(mapper::toDomain).toList();
    }


}
