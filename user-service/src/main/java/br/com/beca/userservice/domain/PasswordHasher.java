package br.com.beca.userservice.domain;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String raw, String hashed);
    boolean isHashed(String value); // 👈 novo
}
