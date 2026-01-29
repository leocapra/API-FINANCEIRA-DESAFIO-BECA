package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountProvisioningGateway;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.application.port.PasswordHasher;
import br.com.beca.userservice.application.usecase.validation.RegexpValidation;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;

import java.util.Optional;

public record CreateUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {

    public User execute(User user)
            throws FieldIsEmptyException, RegexpException {

        if (user.getCpf() == null || user.getCpf().isBlank()) throw new FieldIsEmptyException("cpf");
        if (user.getNome() == null || user.getNome().isBlank()) throw new FieldIsEmptyException("nome");
        if (user.getEmail() == null || user.getEmail().isBlank()) throw new FieldIsEmptyException("email");
        if (user.getSenha() == null || user.getSenha().isBlank()) throw new FieldIsEmptyException("senha");
        if (user.getTelefone() == null || user.getTelefone().isBlank()) throw new FieldIsEmptyException("telefone");

        Optional<User> existingByCpf = userRepository.findByCpf(user.getCpf());
        if (existingByCpf.isPresent()) {
            User existing = existingByCpf.get();
            if (!existing.isActive()) {
                existing.activate();
                return userRepository.updateUser(existing);
            }
            throw new AlreadyExistsException("CPF " + user.getCpf() + " já está cadastrado!");
        }

        Optional<User> existingByEmail = userRepository.findByEmail(user.getEmail());
        if (existingByEmail.isPresent()) {
            User existing = existingByEmail.get();
            if (!existing.isActive()) {
                existing.activate();
                return userRepository.updateUser(existing);
            }
            throw new AlreadyExistsException("Email " + user.getEmail() + " já está cadastrado!");
        }

        if (!RegexpValidation.cpf(user.getCpf())) throw new RegexpException("CPF " + user.getCpf());
        if (!RegexpValidation.email(user.getEmail())) throw new RegexpException("Email " + user.getEmail());
        if (!RegexpValidation.telefone(user.getTelefone())) throw new RegexpException("Telefone " + user.getTelefone());

        user.ensurePasswordHashed(passwordHasher);

        User saved = userRepository.saveUser(user);


        return saved;
    }

}
