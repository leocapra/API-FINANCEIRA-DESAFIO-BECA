package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.application.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean isHashed(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$.*");
    }
}
