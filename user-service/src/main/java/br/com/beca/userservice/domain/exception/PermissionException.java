package br.com.beca.userservice.domain.exception;

public class PermissionException extends RuntimeException {
    public PermissionException(String message){
        super(message);
    }
}
