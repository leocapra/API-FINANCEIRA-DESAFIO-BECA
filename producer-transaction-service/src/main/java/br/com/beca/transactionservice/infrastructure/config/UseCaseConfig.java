package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.CreateDepositUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateDepositUseCase createDepositUseCase(TransactionRepository transactionRepository){
        return new CreateDepositUseCase(transactionRepository);
    }
}
