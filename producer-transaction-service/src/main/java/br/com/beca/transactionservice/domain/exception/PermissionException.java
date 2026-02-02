package br.com.beca.transactionservice.domain.exception;

public class PermissionException extends RuntimeException {
    public PermissionException(String message){
        super(message);
    }
}
