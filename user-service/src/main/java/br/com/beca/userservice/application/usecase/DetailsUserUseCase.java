package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public record DetailsUserUseCase(UserRepository repository) {
    public User execute(UUID id, TokenInfoData tokenData){
        User details = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado ou inativo com ID: " + id));

        if (tokenData.role().equals("ROLE_USER")){
            if (!tokenData.userId().equals(id.toString())){
                throw new PermissionException("Permissão insuficiente para ação!");
            }
        }

        return new User(
                details.getId(),
                details.getCpf(),
                details.getRoles(),
                details.getNome(),
                details.getEmail(),
                details.getSenha(),
                details.getTelefone(),
                details.isActive(),
                details.getCreatedAt(),
                details.getUpdatedAt()
        );
    }
}
