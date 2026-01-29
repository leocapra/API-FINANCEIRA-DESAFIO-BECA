package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.BankAccountPort;
import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.ControllerUseCase;
import br.com.beca.transactionservice.application.usecase.ProcessDepositUseCase;
import br.com.beca.transactionservice.application.usecase.ProcessWithdrawalUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ControllerUseCase controllerUseCase(
            ProcessDepositUseCase processDepositUseCase,
            ProcessWithdrawalUseCase processWithdrawalUseCase
    ) {
        return new ControllerUseCase(
                processDepositUseCase,
                processWithdrawalUseCase
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


}
