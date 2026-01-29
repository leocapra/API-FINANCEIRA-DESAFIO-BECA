package br.com.beca.userservice.application.port;

import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PageDataDomain;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    User saveUser(User user);
    User updateUser(User user);
    PaginatedResponse<User> findAllActive(PageDataDomain pageData);
    Optional<User> findByCpf(String cpf);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndActiveTrue(UUID id);
    void deleteUser(UUID id);
}
