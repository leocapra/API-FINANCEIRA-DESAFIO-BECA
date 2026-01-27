package br.com.beca.userservice.domain.exception;

public class FieldIsEmptyException extends Throwable {
    public FieldIsEmptyException(String field) {
        super("Campo " + field + " não pode ser vazio!");
    }
}
