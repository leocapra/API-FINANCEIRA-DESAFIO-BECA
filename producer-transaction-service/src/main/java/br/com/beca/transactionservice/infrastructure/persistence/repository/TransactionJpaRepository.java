package br.com.beca.transactionservice.infrastructure.persistence.repository;

import br.com.beca.transactionservice.infrastructure.persistence.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
}
