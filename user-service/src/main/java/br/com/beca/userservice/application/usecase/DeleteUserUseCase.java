package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public record DeleteUserUseCase(UserRepository userRepository, BankAccountPort bankAccountPort) {
    public void execute(UUID id, TokenInfoData tokenData) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Não foi possível encontrar usuário com id " + id + " para exclusão!"));

        if (tokenData.role().equals("ROLE_ADMIN")){
            if (tokenData.userId().equals(user.getId().toString())){
                throw new PermissionException("Administrador não pode ser excluído!");
            }
        }

        if (tokenData.role().equals("ROLE_USER")){
            if (!tokenData.userId().equals(id.toString())){
                throw new PermissionException("Permissão insuficiente para ação!");
            }
        }

        userRepository.deleteUser(id);
        bankAccountPort.deleteAccountById(id.toString());
    }
}
