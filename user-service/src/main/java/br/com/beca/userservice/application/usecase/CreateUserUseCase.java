package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.PasswordHasher;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.application.usecase.validation.RegexpValidation;
import br.com.beca.userservice.domain.dto.RequestUserData;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;

public record CreateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher, User user, BankAccountPort bankAccount) {

    public User execute(RequestUserData dto)
            throws FieldIsEmptyException, RegexpException {

        if (dto.cpf() == null || dto.cpf().isBlank()) throw new FieldIsEmptyException("cpf");
        if (dto.nome() == null || dto.nome().isBlank()) throw new FieldIsEmptyException("nome");
        if (dto.email() == null || dto.email().isBlank()) throw new FieldIsEmptyException("email");
        if (dto.senha() == null || dto.senha().isBlank()) throw new FieldIsEmptyException("senha");
        if (dto.telefone() == null || dto.telefone().isBlank()) throw new FieldIsEmptyException("telefone");

        if (!RegexpValidation.cpf(dto.cpf())) throw new RegexpException("CPF " + dto.cpf());
        if (!RegexpValidation.email(dto.email())) throw new RegexpException("Email " + dto.email());
        if (!RegexpValidation.telefone(dto.telefone())) throw new RegexpException("Telefone " + dto.telefone());

        Optional<User> existingByCpf = userRepository.findByCpf(dto.cpf());
        if (existingByCpf.isPresent()) {
            User existing = existingByCpf.get();
            if (!existing.isActive()) {
                existing.activate();
                bankAccount.createAccountForUser(existing.getId().toString(), existing.getEmail());
                return userRepository.updateUser(existing);
            }
            throw new AlreadyExistsException("CPF " + dto.cpf() + " já está cadastrado!");
        }

        Optional<User> existingByEmail = userRepository.findByEmail(dto.email());
        if (existingByEmail.isPresent()) {
            User existing = existingByEmail.get();
            if (!existing.isActive()) {
                existing.activate();
                bankAccount.createAccountForUser(existing.getId().toString(), existing.getEmail());
                return userRepository.updateUser(existing);
            }
            throw new AlreadyExistsException("Email " + dto.email() + " já está cadastrado!");
        }

        User newUser = user.createUser(dto.cpf(), dto.nome(), dto.email().toLowerCase(), dto.senha(), dto.telefone());
        newUser.ensurePasswordHashed(passwordHasher);
        User createdUser = userRepository.saveUser(newUser);
        bankAccount.createAccountForUser(createdUser.getId().toString(), createdUser.getEmail());
        return createdUser;

    }

}
