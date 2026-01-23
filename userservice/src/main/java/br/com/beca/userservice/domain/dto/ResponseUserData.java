package br.com.beca.userservice.domain.dto;

public record ResponseUserData(
        Long id,
        String cpf,
        String nome,
        String email,
        String telefone
) {
}
