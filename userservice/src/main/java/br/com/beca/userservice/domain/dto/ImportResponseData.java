package br.com.beca.userservice.domain.dto;

public record ImportResponseData(
        int totalRows,
        int imported,
        int failed
) {
}
