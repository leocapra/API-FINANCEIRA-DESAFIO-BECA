package br.com.beca.userservice.domain.dto;


public record ImportUserRequestData(
        String nome,
        String cpf,
        String email,
        String senha,
        String telefone
) {}
