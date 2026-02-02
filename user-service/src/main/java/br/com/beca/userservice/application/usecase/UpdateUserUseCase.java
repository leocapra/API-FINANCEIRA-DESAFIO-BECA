package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.application.usecase.validation.RegexpValidation;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.dto.UpdateUserData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;

import java.util.UUID;

public record UpdateUserUseCase(UserRepository userRepository, BankAccountPort bankAccount)  {

    public User execute(UUID id, UpdateUserData dto, TokenInfoData tokenData) throws RegexpException {
        User isExists = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        String nome = dto.nome();
        String email = dto.email() != null ? dto.email().trim() : null;
        String telefone = dto.telefone() != null ? dto.telefone().trim() : null;

        if (email != null && !email.isBlank()) {
            if (!RegexpValidation.email(email)) {
                throw new RegexpException("Email");
            }
        }
        if (telefone != null && !telefone.isBlank()) {
            if (!RegexpValidation.telefone(telefone)) {
                throw new RegexpException("Telefone");
            }
        }

        if ("ROLE_ADMIN".equals(tokenData.role())) {
            if (tokenData.userId().equals(isExists.getId().toString())) {
                throw new PermissionException("Administrador não pode ser editado!");
            }
        } else if ("ROLE_USER".equals(tokenData.role())) {
            if (!tokenData.userId().equals(id.toString())) {
                throw new PermissionException("Permissão insuficiente para ação!");
            }
        } else {
            throw new PermissionException("Permissão insuficiente para ação!");
        }

        User newUser = getUser(nome, email, telefone, isExists);

        return userRepository.updateUser(newUser);
    }

    private User getUser(String nome, String email, String telefone, User isExists) {
        User newUser = new User(
                isExists.getId(),
                isExists.getCpf(),
                isExists.getRoles(),
                isExists.getNome(),
                isExists.getEmail(),
                isExists.getSenha(),
                isExists.getTelefone(),
                isExists.isActive(),
                isExists.getCreatedAt(),
                isExists.getUpdatedAt()
        );

        if (email != null && !email.isBlank()) {
            String emailLower = email.toLowerCase();
            bankAccount.updateAccountEmail(newUser.getId().toString(), emailLower);
        }

        String emailLowerOrNull = (email != null) ? email.toLowerCase() : null;
        newUser.updateProfile(nome, emailLowerOrNull, telefone);

        return newUser;
    }
}