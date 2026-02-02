package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.FilterListTransactionData;
import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.domain.exception.PermissionException;
import br.com.beca.transactionservice.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public record ListTransactionsUseCase(TransactionRepository repository) {


    public List<Transaction> execute(FilterListTransactionData dto, TokenInfoData tokenData) {

        String userId = resolveUserId(dto, tokenData);
        validatePermission(userId, tokenData);

        return repository.search(
                UUID.fromString(userId),
                dto.status(),
                dto.type(),
                dto.startCreatedAt(),
                dto.endCreatedAt()
        );
    }

    private String resolveUserId(FilterListTransactionData dto, TokenInfoData tokenData) {
        if (dto.userId() == null || dto.userId().toString().isBlank()) {
            return tokenData.userId();
        }
        return dto.userId().toString();
    }

    private void validatePermission(String userId, TokenInfoData tokenData) {
        if ("ROLE_USER".equals(tokenData.role())
                && !tokenData.userId().equals(userId)) {
            throw new PermissionException(
                    "Permissão insuficiente para consultar transações de terceiros"
            );
        }

    }
}

