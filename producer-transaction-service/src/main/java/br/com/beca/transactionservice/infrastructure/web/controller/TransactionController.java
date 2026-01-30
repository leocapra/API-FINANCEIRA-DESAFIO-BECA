package br.com.beca.transactionservice.infrastructure.web.controller;

import br.com.beca.transactionservice.application.usecase.CreateBuyUseCase;
import br.com.beca.transactionservice.application.usecase.CreateDepositUseCase;
import br.com.beca.transactionservice.application.usecase.CreateTransferUseCase;
import br.com.beca.transactionservice.application.usecase.CreateWithdrawalUseCase;
import br.com.beca.transactionservice.domain.dto.*;
import br.com.beca.transactionservice.domain.model.Transaction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@SecurityRequirement(name = "bearer-key")
public class TransactionController {
    private final CreateDepositUseCase createDepositUseCase;
    private final CreateWithdrawalUseCase createWithdrawalUseCase;
    private final CreateTransferUseCase createTransferUseCase;
    private final CreateBuyUseCase createBuyUseCase;

    public TransactionController(CreateDepositUseCase createDepositUseCase, CreateWithdrawalUseCase createWithdrawalUseCase, CreateTransferUseCase createTransferUseCase, CreateBuyUseCase createBuyUseCase) {
        this.createDepositUseCase = createDepositUseCase;
        this.createWithdrawalUseCase = createWithdrawalUseCase;
        this.createTransferUseCase = createTransferUseCase;
        this.createBuyUseCase = createBuyUseCase;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@RequestBody TransactionDepositData request){
        Transaction tx = createDepositUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<?> createWithdrawal(@RequestBody TransactionWithdrawalData request){
        Transaction tx = createWithdrawalUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransfer(@RequestBody TransactionTransferData request){
        Transaction tx = createTransferUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy")
    public ResponseEntity<?> createBuy(@RequestBody TransactionBuyData request){
        Transaction tx = createBuyUseCase.execute(request);
        return ResponseEntity.ok().build();
    }
}
