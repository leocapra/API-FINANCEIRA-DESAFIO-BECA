package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;

public record ControllerUseCase(
        ProcessDepositUseCase processDepositUseCase,
        ProcessWithdrawalUseCase processWithdrawalUseCase
) {

    public void execute(TransactionRequestedEvent event) {

        switch (event.type()) {
            case DEPOSITO -> processDepositUseCase.execute(event);
            case SAQUE -> processWithdrawalUseCase.execute(event);
        }

    }
}
