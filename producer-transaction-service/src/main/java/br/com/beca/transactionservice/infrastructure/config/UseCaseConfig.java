package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionPdfWriterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateDepositUseCase createDepositUseCase(TransactionRepository transactionRepository, TransactionEventPublisher publisher){
        return new CreateDepositUseCase(transactionRepository, publisher);
    }

    @Bean
    public CreateWithdrawalUseCase createWithdrawalUseCase(TransactionRepository transactionRepository, TransactionEventPublisher publisher){
        return new CreateWithdrawalUseCase(transactionRepository, publisher);
    }

    @Bean
    public CreateTransferUseCase createTransferUseCase(TransactionRepository transactionRepository, TransactionEventPublisher publisher){
        return new CreateTransferUseCase(transactionRepository, publisher);
    }

    @Bean
    public CreateBuyUseCase createBuyUseCase(TransactionRepository transactionRepository, TransactionEventPublisher publisher){
        return new CreateBuyUseCase(transactionRepository, publisher);
    }

    @Bean
    public CancelTransactionUseCase cancelTransactionUseCase(TransactionRepository transactionRepository){
        return new CancelTransactionUseCase(transactionRepository);
    }

    @Bean
    public ListTransactionsUseCase listTransactionsUseCase(TransactionRepository transactionRepository){
        return new ListTransactionsUseCase(transactionRepository);
    }

    @Bean
    public ExportPdfTransactionsUseCase exportPdfTransactionsUseCase(TransactionRepository transactionRepository, TransactionPdfWriterPort pdfWriter){
        return new ExportPdfTransactionsUseCase(transactionRepository, pdfWriter);
    }


}
