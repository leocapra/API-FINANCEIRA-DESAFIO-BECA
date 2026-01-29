package br.com.beca.transactionservice.infrastructure.web.controller;

import br.com.beca.transactionservice.application.usecase.CreateDepositUseCase;
import br.com.beca.transactionservice.application.usecase.CreateWithdrawalUseCase;
import br.com.beca.transactionservice.domain.dto.TransactionRequestData;
import br.com.beca.transactionservice.domain.model.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final CreateDepositUseCase createDepositUseCase;
    private final CreateWithdrawalUseCase createWithdrawalUseCase;

    public TransactionController(CreateDepositUseCase createDepositUseCase, CreateWithdrawalUseCase createWithdrawalUseCase) {
        this.createDepositUseCase = createDepositUseCase;
        this.createWithdrawalUseCase = createWithdrawalUseCase;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@RequestBody TransactionRequestData request){
        Transaction tx = createDepositUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<?> createWithdrawal(@RequestBody TransactionRequestData request){
        Transaction tx = createWithdrawalUseCase.execute(request);
        return ResponseEntity.ok().build();
    }
}
