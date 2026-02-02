package br.com.beca.userservice.domain.dto;

public record TokenInfoData(
        String userId,
        String role
) {
}
