package br.com.beca.userservice.domain.dto;

public record UpdateUserData(
        String nome,
        String email,
        String telefone
) { }
