package br.com.beca.userservice.domain.exception;

public class RegexpException extends Throwable {
    public RegexpException(String message) {
        super(message + " não tem um formato válido!");
    }
}
