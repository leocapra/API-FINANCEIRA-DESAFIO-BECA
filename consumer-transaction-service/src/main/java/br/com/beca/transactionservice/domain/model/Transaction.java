package br.com.beca.transactionservice.domain.model;

import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private final UUID userId;
    private final TransactionType type;
    private final Money amount;
    private final AccountRef sourceAccount;
    private final UUID targetAccount;
    private TransactionStatus status;
    private String description;
    private String category;
    private String rejectionReason;
    private final LocalDateTime createdAt;
    private LocalDateTime processAt;
    private final String correlationId;
    private BigDecimal brl;
    private BigDecimal fxRate;
    private Boolean record;
    private TransferType transferType;
    private BuyType buyType;

    public Transaction(
            UUID id,
            UUID userId,
            TransactionType type,
            Money amount,
            AccountRef sourceAccount,
            UUID targetAccount,
            TransactionStatus status,
            String description,
            String category,
            String rejectionReason,
            LocalDateTime createdAt,
            LocalDateTime processAt,
            String correlationId,
            BigDecimal brl,
            BigDecimal fxRate,
            Boolean record,
            TransferType transferType,
            BuyType buyType
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.status = status;
        this.description = description;
        this.category = category;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.processAt = processAt;
        this.correlationId = correlationId;
        this.brl = brl;
        this.fxRate = fxRate;
        this.record = record;
        this.transferType = transferType;
        this.buyType = buyType;
    }

    public void approve() {
        this.status = TransactionStatus.APROVADA;
        this.processAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = TransactionStatus.REJEITADA;
        this.rejectionReason = reason;
        this.processAt = LocalDateTime.now();
    }

    public void toBrl(BigDecimal brl, BigDecimal fxRate){
        this.brl = brl;
        this.fxRate = fxRate;
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

    public Money getAmount() {
        return amount;
    }

    public AccountRef getSourceAccount() {
        return sourceAccount;
    }

    public UUID getTargetAccount() {
        return targetAccount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessAt() {
        return processAt;
    }

    public String getCorrelationId() {
        return correlationId;
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
