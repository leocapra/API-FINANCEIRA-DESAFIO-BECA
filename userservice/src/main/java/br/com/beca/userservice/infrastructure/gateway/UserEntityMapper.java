package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserJpa toEntity(User user){
        return new UserJpa(
                user.getCpf(),
                user.getNome(),
                user.getEmail(),
                user.getSenha(),
                user.getTelefone(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
                );
    }

    public User toDomain(UserJpa userJpa) {
        return new User(
                userJpa.getId(),
                userJpa.getCpf(),
                userJpa.getNome(),
                userJpa.getEmail(),
                userJpa.getSenha(),
                userJpa.getTelefone(),
                userJpa.isActive(),
                userJpa.getCreatedAt(),
                userJpa.getUpdatedAt());
    }

}
