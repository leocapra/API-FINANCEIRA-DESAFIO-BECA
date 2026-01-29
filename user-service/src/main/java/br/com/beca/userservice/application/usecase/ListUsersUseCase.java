package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PageDataDomain;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;


public record ListUsersUseCase(UserRepository userRepository) {

    public PaginatedResponse<User> execute(PageDataDomain pageData) {
        return userRepository.findAllActive(pageData);
    }
}
