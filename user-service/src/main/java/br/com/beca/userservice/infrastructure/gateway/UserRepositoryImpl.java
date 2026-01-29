package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.application.port.BankAccountProvisioningGateway;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PageDataDomain;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import br.com.beca.userservice.infrastructure.persistence.repository.UserRepositoryJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserRepositoryJpa repository;
    private final UserEntityMapper mapper;
    private final BankAccountProvisioningGateway bankGateway;

    public UserRepositoryImpl(UserRepositoryJpa repository, UserEntityMapper mapper, BankAccountProvisioningGateway bankGateway) {
        this.repository = repository;
        this.mapper = mapper;
        this.bankGateway = bankGateway;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return repository.existsByCpf(cpf);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        UserJpa entity = mapper.toEntity(user);
        entity.setCreatedAt(LocalDate.now());
        entity.setUpdatedAt(LocalDate.now());

        UserJpa saved = repository.save(entity);
        bankGateway.createAccountForUser(saved.getId().toString(), saved.getEmail());

        return mapper.toDomain(saved);
    }

    @Override
    public User updateUser(User user) {
        UserJpa existing = repository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        existing.updateProfile(
                user.getNome(),
                user.getEmail(),
                user.getTelefone()
        );
        existing.setUpdatedAt(LocalDate.now());
        UserJpa saved = repository.save(existing);
        return mapper.toDomain(saved);
    }

    @Override
    public PaginatedResponse<User> findAllActive(PageDataDomain pageData) {
        Pageable pageable = PageRequest.of(
                pageData.getPage(),
                pageData.getSize(),
                new PageSortMapper().domainToSort(pageData.getSort())
        );
        Page<UserJpa> pageJpa = repository.findAllByActiveTrue(pageable);
        Page<User> pageDomain = pageJpa.map(mapper::toDomain);
        return new PageMapper().converter(pageDomain);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return repository.findByCpf(cpf).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdAndActiveTrue(UUID id) {
        Optional<UserJpa> userJpa = repository.findByIdAndActiveTrue(id);
        Optional<User> user = userJpa.map(mapper::toDomain);
        return user;
    }

    @Override
    public void deleteUser(UUID id) {
        Optional<UserJpa> find = repository.findByIdAndActiveTrue(id);
        find.get().setUpdatedAt(LocalDate.now());
        find.get().deactivate();

    }

}
