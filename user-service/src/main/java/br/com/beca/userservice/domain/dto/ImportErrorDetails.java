package br.com.beca.userservice.domain.dto;

public record ImportErrorDetails(
        String identifier,
        String message
) {}