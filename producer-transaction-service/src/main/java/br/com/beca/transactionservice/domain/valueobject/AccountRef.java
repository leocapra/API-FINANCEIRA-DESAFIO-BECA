package br.com.beca.transactionservice.domain.valueobject;

import java.util.UUID;

public record AccountRef(UUID accountId) {
    public AccountRef {
        if (accountId == null){
            throw new IllegalArgumentException("AccountId é obrigatório!");
        }
    }
}
