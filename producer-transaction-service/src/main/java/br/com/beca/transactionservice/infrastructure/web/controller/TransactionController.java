package br.com.beca.transactionservice.infrastructure.web.controller;

import br.com.beca.transactionservice.application.usecase.*;
import br.com.beca.transactionservice.domain.dto.*;
import br.com.beca.transactionservice.domain.model.Transaction;
import br.com.beca.transactionservice.infrastructure.web.service.ExtractInfoFromToken;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@SecurityRequirement(name = "bearer-key")
public class TransactionController {
    private final CreateDepositUseCase createDepositUseCase;
    private final CreateWithdrawalUseCase createWithdrawalUseCase;
    private final CreateTransferUseCase createTransferUseCase;
    private final CreateBuyUseCase createBuyUseCase;
    private final ExtractInfoFromToken extractInfoFromToken;
    private final CancelTransactionUseCase cancelTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final ExportPdfTransactionsUseCase exportPdfTransactionsUseCase;

    public TransactionController(
            CreateDepositUseCase createDepositUseCase,
            CreateWithdrawalUseCase createWithdrawalUseCase,
            CreateTransferUseCase createTransferUseCase,
            CreateBuyUseCase createBuyUseCase,
            ExtractInfoFromToken extractInfoFromToken,
            CancelTransactionUseCase cancelTransactionUseCase, ListTransactionsUseCase listTransactionsUseCase, ExportPdfTransactionsUseCase exportPdfTransactionsUseCase
    ) {
        this.createDepositUseCase = createDepositUseCase;
        this.createWithdrawalUseCase = createWithdrawalUseCase;
        this.createTransferUseCase = createTransferUseCase;
        this.createBuyUseCase = createBuyUseCase;
        this.extractInfoFromToken = extractInfoFromToken;
        this.cancelTransactionUseCase = cancelTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.exportPdfTransactionsUseCase = exportPdfTransactionsUseCase;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionDepositData> createDeposit(@RequestBody TransactionDepositData body, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        Transaction tx = createDepositUseCase.execute(body, tokenData);
        return ResponseEntity.ok(new TransactionDepositData(tx.getAmount().value(), tx.getAmount().currency(), tx.getRecord() != null));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionWithdrawalData> createWithdrawal(@RequestBody TransactionWithdrawalData body, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        Transaction tx = createWithdrawalUseCase.execute(body, tokenData);
        return ResponseEntity.ok(new TransactionWithdrawalData(tx.getAmount().value(), tx.getAmount().currency(), tx.getRecord() != null));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionTransferData> createTransfer(@RequestBody TransactionTransferData body, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        Transaction tx = createTransferUseCase.execute(body, tokenData);
        return ResponseEntity.ok(new TransactionTransferData(tx.getAmount().value(), tx.getAmount().currency(), tx.getTargetAccount(), tx.getTransferType(), tx.getRecord() != null));
    }

    @PostMapping("/buy")
    public ResponseEntity<TransactionBuyData> createBuy(@RequestBody TransactionBuyData body, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        Transaction tx = createBuyUseCase.execute(body, tokenData);
        return ResponseEntity.ok(new TransactionBuyData(tx.getAmount().value(), tx.getAmount().currency(), tx.getDescription(), tx.getCategory(), tx.getBuyType(), tx.getRecord() != null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelTransaction(@PathVariable UUID id, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        cancelTransactionUseCase.execute(id, tokenData);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<Transaction>> listTransactionsTransaction(@RequestBody FilterListTransactionData body, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        List<Transaction> list = listTransactionsUseCase.execute(body, tokenData);
        return ResponseEntity.ok(list);
    }


    @PostMapping(
            path = "/export",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> exportPdfTransactions(@RequestBody FilterListTransactionData body, HttpServletRequest request) throws IOException {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        byte[] pdf = exportPdfTransactionsUseCase.execute(body, tokenData);
        String filename = "extrato-transacoes.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
