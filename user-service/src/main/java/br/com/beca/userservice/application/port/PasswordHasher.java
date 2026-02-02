package br.com.beca.userservice.application.port;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean isHashed(String value);
}
