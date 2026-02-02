package br.com.beca.transactionservice.domain.exception;

public class FieldIsException extends RuntimeException {
    public FieldIsException(String field) {
        super(field);
    }
}
