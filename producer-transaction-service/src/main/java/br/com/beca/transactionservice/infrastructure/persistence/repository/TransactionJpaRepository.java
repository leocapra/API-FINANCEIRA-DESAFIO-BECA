package br.com.beca.transactionservice.infrastructure.persistence.repository;

import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.infrastructure.persistence.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE (:userId IS NULL OR t.userId = :userId)
              AND (:status IS NULL OR t.status = :status)
              AND (:type IS NULL OR t.type = :type)
              AND (CAST(:startCreatedAt AS timestamp) IS NULL OR t.createdAt >= :startCreatedAt)
              AND (CAST(:endCreatedAt AS timestamp) IS NULL OR t.createdAt <  :endCreatedAt)
            """)
    List<TransactionEntity> search(
            @Param("userId") UUID userId,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            @Param("startCreatedAt") LocalDateTime startCreatedAt,
            @Param("endCreatedAt") LocalDateTime endCreatedAt
    );
}

