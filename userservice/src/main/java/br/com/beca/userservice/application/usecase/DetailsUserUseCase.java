package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;

public record DetailsUserUseCase(UserRepository repository) {
    public Optional<User> execute(Long id){
        Optional<User> details = repository.findByIdAndActiveTrue(id);
        return details;
    }
}
