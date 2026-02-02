package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionPdfWriterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.FilterListTransactionData;
import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.domain.exception.PermissionException;
import br.com.beca.transactionservice.domain.model.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public record ExportPdfTransactionsUseCase(
        TransactionRepository repository,
        TransactionPdfWriterPort pdfWriter // <-- injeta a port
) {

    public byte[] execute(FilterListTransactionData dto, TokenInfoData tokenData) throws IOException {
        String userId = resolveUserId(dto, tokenData);
        validatePermission(userId, tokenData);

        List<Transaction> transactions = repository.search(
                UUID.fromString(userId),
                dto.status(),
                dto.type(),
                dto.startCreatedAt(),
                dto.endCreatedAt()
        );

        return pdfWriter.write(transactions);
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