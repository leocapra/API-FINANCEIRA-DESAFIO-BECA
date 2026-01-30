package br.com.beca.transactionservice.application.usecase;

import br.com.beca.transactionservice.domain.event.TransactionRequestedEvent;

public record ControllerUseCase(
        ProcessDepositUseCase processDepositUseCase,
        ProcessWithdrawalUseCase processWithdrawalUseCase,
        ProcessTransferUseCase processTransferUseCase,
        ProcessBuyUseCase processBuyUseCase
) {

    public void execute(TransactionRequestedEvent event) {

        switch (event.type()) {
            case DEPOSITO -> processDepositUseCase.execute(event);
            case SAQUE -> processWithdrawalUseCase.execute(event);
            case TRANSFERENCIA -> processTransferUseCase.execute(event);
            case COMPRA -> processBuyUseCase.execute(event);
        }

    }
}
