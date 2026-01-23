package br.com.beca.userservice.domain.dto;


public record RequestUserData(
        String cpf,
        String nome,
        String email,
        String senha,
        String telefone
        ) { }
