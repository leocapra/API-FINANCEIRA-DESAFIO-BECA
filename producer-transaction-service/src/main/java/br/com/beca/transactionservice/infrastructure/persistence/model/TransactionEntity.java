package br.com.beca.transactionservice.infrastructure.persistence.model;

import br.com.beca.transactionservice.domain.model.BuyType;
import br.com.beca.transactionservice.domain.model.TransactionStatus;
import br.com.beca.transactionservice.domain.model.TransactionType;
import br.com.beca.transactionservice.domain.model.TransferType;
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

    private BigDecimal brl;

    private BigDecimal fxRate;

    private Boolean record;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    private BuyType buyType;


    public TransactionEntity() {}

    public TransactionEntity(
            UUID id,
            UUID userId,
            TransactionType type,
            TransactionStatus status,
            BigDecimal amount,
            String currency,
            UUID sourceAccountId,
            UUID targetAccountId,
            String description,
            String category,
            Instant createdAt,
            Instant processedAt,
            String correlationId,
            String rejectionReason,
            BigDecimal brl,
            BigDecimal fxRate,
            Boolean record,
            TransferType transferType,
            BuyType buyType
    ) {
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
        this.brl = brl;
        this.fxRate = fxRate;
        this.record = record;
        this.transferType = transferType;
        this.buyType = buyType;
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

    public BigDecimal getBrl() {
        return brl;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public Boolean getRecord() {
        return record;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public BuyType getBuyType() {
        return buyType;
    }
}
