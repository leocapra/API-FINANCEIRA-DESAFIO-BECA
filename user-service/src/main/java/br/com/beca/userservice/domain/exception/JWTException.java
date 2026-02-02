package br.com.beca.userservice.domain.exception;

public class JWTException extends RuntimeException {
    public JWTException(String message){
        super(message);
    }
}
