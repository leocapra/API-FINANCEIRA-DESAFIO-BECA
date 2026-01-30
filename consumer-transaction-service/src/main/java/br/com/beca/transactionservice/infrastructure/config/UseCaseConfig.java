package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ControllerUseCase controllerUseCase(
            ProcessDepositUseCase processDepositUseCase,
            ProcessWithdrawalUseCase processWithdrawalUseCase,
            ProcessTransferUseCase processTransferUseCase,
            ProcessBuyUseCase processBuyUseCase
    ) {
        return new ControllerUseCase(
                processDepositUseCase,
                processWithdrawalUseCase,
                processTransferUseCase,
                processBuyUseCase
        );
    }

    @Bean
    public ProcessDepositUseCase processDepositUseCase(TransactionRepository repository, BankAccountPort deposit, CurrencyConverterPort converter) {
        return new ProcessDepositUseCase(repository, deposit, converter);
    }

    @Bean
    public ProcessWithdrawalUseCase processWithdrawalUseCase(TransactionRepository repository, BankAccountPort deposit, CurrencyConverterPort converter) {
        return new ProcessWithdrawalUseCase(repository, deposit, converter);
    }

    @Bean
    public ProcessTransferUseCase processTransferUseCase(TransactionRepository repository, BankAccountPort deposit, CurrencyConverterPort converter) {
        return new ProcessTransferUseCase(repository, deposit, converter);
    }

    @Bean
    public ProcessBuyUseCase processBuyUseCase(TransactionRepository repository, BankAccountPort deposit, CurrencyConverterPort converter) {
        return new ProcessBuyUseCase(repository, deposit, converter);
    }

}
