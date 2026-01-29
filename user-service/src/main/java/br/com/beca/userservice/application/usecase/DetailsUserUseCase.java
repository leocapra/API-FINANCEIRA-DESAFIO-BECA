package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public record DetailsUserUseCase(UserRepository repository) {
    public Optional<User> execute(UUID id){
        Optional<User> details = repository.findByIdAndActiveTrue(id);
        return details;
    }
}
