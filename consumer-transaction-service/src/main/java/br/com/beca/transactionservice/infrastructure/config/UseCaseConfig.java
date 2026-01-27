package br.com.beca.transactionservice.infrastructure.config;

import br.com.beca.transactionservice.application.port.TransactionEventPublisher;
import br.com.beca.transactionservice.application.port.TransactionRepository;
import br.com.beca.transactionservice.application.usecase.ProcessDepositUseCase;
import br.com.beca.transactionservice.infrastructure.web.kafkalistener.TransactionRequestedListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcessDepositUseCase processDepositUseCase(TransactionRepository repo) {
        return new ProcessDepositUseCase(repo);
    }


}
