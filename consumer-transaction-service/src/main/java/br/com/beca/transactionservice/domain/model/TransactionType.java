package br.com.beca.transactionservice.domain.model;

public enum TransactionType {
    DEPOSITO,
    SAQUE,
    PIX,
    TRANSFERENCIA,
    COMPRA;

    public boolean requiresTargetAccount(){
        return this == PIX || this == TRANSFERENCIA;
    }
}
