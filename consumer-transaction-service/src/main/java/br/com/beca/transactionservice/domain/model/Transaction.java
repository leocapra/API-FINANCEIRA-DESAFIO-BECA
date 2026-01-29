package br.com.beca.transactionservice.domain.model;

import br.com.beca.transactionservice.domain.valueobject.AccountRef;
import br.com.beca.transactionservice.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.Instant;
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
    private final Instant createdAt;
    private Instant processAt;
    private final String correlationId;
    private BigDecimal brl;
    private BigDecimal fxRate;

    private Transaction(
            UUID id,
            UUID userId,
            TransactionType type,
            Money amount,
            AccountRef sourceAccount,
            UUID targetAccount,
            String description,
            String category,
            Instant createdAt,
            String correlationId
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.status = TransactionStatus.PENDENTE;
        this.description = description;
        this.category = category;
        this.createdAt = createdAt;
        this.correlationId = correlationId;

        validateRules();
    }

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
            Instant createdAt,
            Instant processAt,
            String correlationId,
            BigDecimal brl,
            BigDecimal fxRate
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
    }

    public static Transaction createPending(
            UUID userId,
            TransactionType type,
            Money amount,
            AccountRef sourceAccount,
            UUID targetAccount,
            String description,
            String category
    ){
        return new Transaction(
                UUID.randomUUID(),
                userId,
                type,
                amount,
                sourceAccount,
                targetAccount,
                description,
                category,
                Instant.now(),
                UUID.randomUUID().toString()
        );
    }

    public void approve() {
        this.status = TransactionStatus.APROVADA;
        this.processAt = Instant.now();
    }

    public void reject(String reason) {
        this.status = TransactionStatus.REJEITADA;
        this.rejectionReason = reason;
        this.processAt = Instant.now();
    }

    private void validateRules(){
        if (userId == null) throw new IllegalArgumentException("UserId é obrigatório!");
        if (type == null) throw new IllegalArgumentException("Transaction Type é obrigatório!");
        if (sourceAccount == null) throw new IllegalArgumentException("Source account é obrigatório!");

        if (type.requiresTargetAccount() && targetAccount == null){
            throw new IllegalArgumentException(type + " conta destino é obrigatória!");
        }
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessAt() {
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
}
