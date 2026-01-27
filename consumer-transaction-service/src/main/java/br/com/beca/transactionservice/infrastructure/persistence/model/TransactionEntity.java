package br.com.beca.transactionservice.infrastructure.persistence.model;

import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private UUID sourceAccountId;

    private UUID targetAccountId;

    private String description;
    private String category;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    @Column(nullable = false)
    private String correlationId;

    private String rejectionReason;

    public TransactionEntity() {}

    public TransactionEntity(UUID id, UUID userId, TransactionType type, TransactionStatus status, BigDecimal amount, String currency, UUID sourceAccountId, UUID targetAccountId, String description, String category, Instant createdAt, Instant processedAt, String correlationId, String rejectionReason) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.correlationId = correlationId;
        this.rejectionReason = rejectionReason;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
