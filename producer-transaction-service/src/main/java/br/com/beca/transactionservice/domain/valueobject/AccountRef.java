package br.com.beca.transactionservice.domain.valueobject;

public record AccountRef(String accountId) {
    public AccountRef {
        if (accountId == null || accountId.isBlank()){
            throw new IllegalArgumentException("AccountId é obrigatório!");
        }
    }
}
