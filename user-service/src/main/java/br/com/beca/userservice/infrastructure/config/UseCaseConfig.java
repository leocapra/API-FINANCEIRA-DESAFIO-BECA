package br.com.beca.userservice.infrastructure.config;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.application.usecase.*;
import br.com.beca.userservice.domain.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new CreateUserUseCase(userRepository, passwordHasher);
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
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public ImportUserFromExcelUseCase importUserFromExcelUseCase(CreateUserUseCase createUserUseCase) {
        return new ImportUserFromExcelUseCase(createUserUseCase);
    }
}
