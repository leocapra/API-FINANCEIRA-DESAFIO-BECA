package br.com.beca.userservice.infrastructure.persistence.repository;

import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


public interface UserRepositoryJpa extends JpaRepository<UserJpa, Long> {
    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Page<UserJpa> findAllByActiveTrue(Pageable pageable);

    Optional<UserJpa> findByCpf(String cpf);

    Optional<UserJpa> findByEmail(String email);

    Optional<UserJpa> findByActiveTrueAndCpf(String cpf);

    Optional<UserJpa> findByIdAndActiveTrue(Long id);

}
