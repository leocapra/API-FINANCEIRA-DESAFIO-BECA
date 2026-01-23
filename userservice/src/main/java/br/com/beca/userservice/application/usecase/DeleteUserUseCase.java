package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;

public record DeleteUserUseCase(UserRepository userRepository) {
    public void execute(Long id) {
        Optional<User> findUser = userRepository.findByIdAndActiveTrue(id);
        if (findUser.isEmpty()){
            throw new NotFoundException("Não foi possível encontrar usuário com id " + id + " para exclusão!");
        }

        userRepository.deleteUser(id);
    }
}
