package br.com.beca.userservice.infrastructure.config;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.application.usecase.*;
import br.com.beca.userservice.application.port.PasswordHasher;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public User user(){
        return new User();
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher, User user, BankAccountPort bankAccount) {
        return new CreateUserUseCase(userRepository, passwordHasher, user, bankAccount);
    }

    @Bean
    public ListUsersUseCase getUserUseCase(UserRepository userRepository) {
        return new ListUsersUseCase(userRepository);
    }

    @Bean
    public DetailsUserUseCase detailsUserUseCase(UserRepository userRepository) {
        return new DetailsUserUseCase(userRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository, BankAccountPort bankAccount) {
        return new UpdateUserUseCase(userRepository, bankAccount);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository, BankAccountPort bankAccountPort) {
        return new DeleteUserUseCase(userRepository, bankAccountPort);
    }

    @Bean
    public ImportUserFromExcelUseCase importUserFromExcelUseCase(CreateUserUseCase createUserUseCase ) {
        return new ImportUserFromExcelUseCase(createUserUseCase);
    }
}
