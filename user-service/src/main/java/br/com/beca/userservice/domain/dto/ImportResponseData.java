package br.com.beca.userservice.domain.dto;

import java.util.List;

// DTO para o erro individual


public record ImportResponseData(
        int totalRows,
        int imported,
        int failed,
        List<ImportErrorDetails> errors
) {}