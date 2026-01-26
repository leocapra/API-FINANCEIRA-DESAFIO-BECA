package br.com.beca.userservice.domain.dto;

import java.util.UUID;

public record ResponseUserData(
        UUID id,
        String cpf,
        String nome,
        String email,
        String telefone
) {
}
