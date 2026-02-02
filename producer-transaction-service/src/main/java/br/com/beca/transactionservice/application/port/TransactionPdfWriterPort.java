package br.com.beca.transactionservice.application.port;

import br.com.beca.transactionservice.domain.dto.FilterListTransactionData;
import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.domain.model.Transaction;

import java.io.IOException;
import java.util.List;

public interface TransactionPdfWriterPort {
    byte[] write(List<Transaction> transactions) throws IOException;
}