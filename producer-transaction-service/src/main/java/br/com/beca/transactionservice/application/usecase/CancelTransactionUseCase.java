package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.domain.exception.FieldIsException;
import br.com.beca.transactionservice.domain.exception.NotFoundException;
import br.com.beca.transactionservice.domain.exception.PermissionException;
import br.com.beca.transactionservice.domain.model.Transaction;

import java.util.UUID;

public record CancelTransactionUseCase(TransactionRepository repository) {

    public void execute(UUID id, TokenInfoData tokenData) {
        if (id == null) throw new FieldIsException("Parametro de ID não pode ser nulo");
        Transaction transaction = repository.findById(id).orElseThrow(() -> new NotFoundException("Não foi possível encontrar transação com o id " + id));

        if (tokenData.role().equals("ROLE_USER")){
            if (!tokenData.userId().equals(transaction.getUserId().toString())){
                throw new PermissionException("Permissão insuficiente para ação");
            }
        }

        transaction.cancel();
        repository.save(transaction);

    }
}
