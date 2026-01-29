package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.application.usecase.validation.RegexpValidation;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.dto.UpdateUserData;

import java.util.Optional;
import java.util.UUID;

public record UpdateUserUseCase(UserRepository userRepository)  {
    public User execute(UUID id, UpdateUserData dto) throws RegexpException {
        Optional<User> isExists = userRepository.findByIdAndActiveTrue(id);
        User user = isExists.map(u -> new User(
                u.getId(),
                u.getCpf(),
                u.getNome(),
                u.getEmail(),
                u.getSenha(),
                u.getTelefone()
                )).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (dto.email() != null && !dto.email().isBlank()) {
            if (!RegexpValidation.email(dto.email())) {
                throw new RegexpException("Email");
            }
        }

        if (dto.telefone() != null && !dto.telefone().isBlank()) {
            if (!RegexpValidation.telefone(dto.telefone())) {
                throw new RegexpException("Telefone");
            }
        }

        user.updateProfile(dto.nome(), dto.email(), dto.telefone());

        return userRepository.updateUser(user);
    }
}
