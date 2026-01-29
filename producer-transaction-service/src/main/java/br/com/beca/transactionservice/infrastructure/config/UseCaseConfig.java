package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.CreateDepositUseCase;
import br.com.beca.transactionservice.application.usecase.CreateWithdrawalUseCase;
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
}
