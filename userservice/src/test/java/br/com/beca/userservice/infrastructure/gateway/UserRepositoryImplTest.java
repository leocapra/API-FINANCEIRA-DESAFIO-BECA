package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import br.com.beca.userservice.infrastructure.persistence.repository.UserRepositoryJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserRepositoryJpa repository;

    @Mock
    private UserEntityMapper mapper;

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl(repository, mapper);
    }

    private User validDomainUser() {
        return new User(
                10L,
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "senhaHash",
                "+55 11 91234-5678"
        );
    }

    private UserJpa validJpaEntity() {
        UserJpa jpa = new UserJpa();
        jpa.setId(10L);
        // se tiver setters pros campos, pode setar aqui também
        return jpa;
    }

    // -------------------------
    // existsByCpf / existsByEmail
    // -------------------------

    @Test
    void existsByCpf_shouldDelegateToJpaRepository() {
        when(repository.existsByCpf("123")).thenReturn(true);

        boolean result = userRepository.existsByCpf("123");

        assertTrue(result);
        verify(repository).existsByCpf("123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void existsByEmail_shouldDelegateToJpaRepository() {
        when(repository.existsByEmail("a@b.com")).thenReturn(false);

        boolean result = userRepository.existsByEmail("a@b.com");

        assertFalse(result);
        verify(repository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(repository);
    }

    // -------------------------
    // findByCpf / findByEmail
    // -------------------------

    @Test
    void findByCpf_shouldMapJpaToDomain() {
        UserJpa jpa = validJpaEntity();
        User domain = validDomainUser();

        when(repository.findByCpf("123.456.789-09")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<User> result = userRepository.findByCpf("123.456.789-09");

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(repository).findByCpf("123.456.789-09");
        verify(mapper).toDomain(jpa);
    }

    @Test
    void findByEmail_shouldMapJpaToDomain() {
        UserJpa jpa = validJpaEntity();
        User domain = validDomainUser();

        when(repository.findByEmail("leo@email.com")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<User> result = userRepository.findByEmail("leo@email.com");

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(repository).findByEmail("leo@email.com");
        verify(mapper).toDomain(jpa);
    }

    @Test
    void findByIdAndActiveTrue_shouldMapJpaToDomain() {
        Long id = 10L;
        UserJpa jpa = validJpaEntity();
        User domain = validDomainUser();

        when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<User> result = userRepository.findByIdAndActiveTrue(id);

        assertTrue(result.isPresent());
        assertSame(domain, result.get());

        verify(repository).findByIdAndActiveTrue(id);
        verify(mapper).toDomain(jpa);
    }

    // -------------------------
    // saveUser
    // -------------------------

    @Test
    void saveUser_shouldSetIdFromCpfIfExists_thenSaveAndReturnMappedDomain() {
        User domainIn = new User("123.456.789-09", "Leonardo", "leo@email.com", "senha", "+55 11 91234-5678");
        UserJpa entity = new UserJpa(); // id ainda null
        UserJpa existingByCpf = new UserJpa();
        existingByCpf.setId(99L);

        User domainOut = new User(99L, domainIn.getCpf(), domainIn.getNome(), domainIn.getEmail(), domainIn.getSenha(), domainIn.getTelefone());

        when(mapper.toEntity(domainIn)).thenReturn(entity);
        when(repository.findByCpf(domainIn.getCpf())).thenReturn(Optional.of(existingByCpf));
        when(repository.findByEmail(domainIn.getEmail())).thenReturn(Optional.empty());

        when(mapper.toDomain(entity)).thenReturn(domainOut);

        User saved = userRepository.saveUser(domainIn);

        // capturar o que foi enviado pro save
        ArgumentCaptor<UserJpa> captor = ArgumentCaptor.forClass(UserJpa.class);
        verify(repository).save(captor.capture());

        assertEquals(99L, captor.getValue().getId());
        assertSame(domainOut, saved);

        verify(mapper).toEntity(domainIn);
        verify(repository).findByCpf(domainIn.getCpf());
        verify(repository).findByEmail(domainIn.getEmail());
        verify(mapper).toDomain(entity);
    }

    @Test
    void saveUser_shouldSetIdFromEmailIfExists_thenSaveAndReturnMappedDomain() {
        User domainIn = new User("123.456.789-09", "Leonardo", "leo@email.com", "senha", "+55 11 91234-5678");
        UserJpa entity = new UserJpa(); // id ainda null
        UserJpa existingByEmail = new UserJpa();
        existingByEmail.setId(77L);

        User domainOut = new User(77L, domainIn.getCpf(), domainIn.getNome(), domainIn.getEmail(), domainIn.getSenha(), domainIn.getTelefone());

        when(mapper.toEntity(domainIn)).thenReturn(entity);
        when(repository.findByCpf(domainIn.getCpf())).thenReturn(Optional.empty());
        when(repository.findByEmail(domainIn.getEmail())).thenReturn(Optional.of(existingByEmail));

        when(mapper.toDomain(entity)).thenReturn(domainOut);

        User saved = userRepository.saveUser(domainIn);

        ArgumentCaptor<UserJpa> captor = ArgumentCaptor.forClass(UserJpa.class);
        verify(repository).save(captor.capture());

        assertEquals(77L, captor.getValue().getId());
        assertSame(domainOut, saved);
    }

    @Test
    void saveUser_shouldSaveWithoutSettingId_whenNoCpfOrEmailFound() {
        User domainIn = new User("123.456.789-09", "Leonardo", "leo@email.com", "senha", "+55 11 91234-5678");
        UserJpa entity = new UserJpa(); // id null
        User domainOut = new User(null, domainIn.getCpf(), domainIn.getNome(), domainIn.getEmail(), domainIn.getSenha(), domainIn.getTelefone());

        when(mapper.toEntity(domainIn)).thenReturn(entity);
        when(repository.findByCpf(domainIn.getCpf())).thenReturn(Optional.empty());
        when(repository.findByEmail(domainIn.getEmail())).thenReturn(Optional.empty());
        when(mapper.toDomain(entity)).thenReturn(domainOut);

        User saved = userRepository.saveUser(domainIn);

        ArgumentCaptor<UserJpa> captor = ArgumentCaptor.forClass(UserJpa.class);
        verify(repository).save(captor.capture());

        assertNull(captor.getValue().getId());
        assertSame(domainOut, saved);
    }

    // -------------------------
    // updateUser
    // -------------------------

    @Test
    void updateUser_shouldUpdateProfileAndSave_whenUserExists() {
        User domainIn = new User(
                10L,
                "123.456.789-09",
                "Nome Novo",
                "novo@email.com",
                "senhaHash",
                "+55 11 91234-5678"
        );

        UserJpa existingJpa = spy(new UserJpa());
        existingJpa.setId(10L);

        UserJpa savedJpa = existingJpa; // simulando retorno do save
        User domainOut = domainIn;

        when(repository.findById(10L)).thenReturn(Optional.of(existingJpa));
        when(repository.save(existingJpa)).thenReturn(savedJpa);
        when(mapper.toDomain(savedJpa)).thenReturn(domainOut);

        User result = userRepository.updateUser(domainIn);

        assertSame(domainOut, result);

        verify(existingJpa).updateProfile(domainIn.getNome(), domainIn.getEmail(), domainIn.getTelefone());
        verify(repository).findById(10L);
        verify(repository).save(existingJpa);
        verify(mapper).toDomain(savedJpa);
    }

    @Test
    void updateUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
        User domainIn = validDomainUser();

        when(repository.findById(domainIn.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userRepository.updateUser(domainIn));
        assertEquals("Usuário não encontrado", ex.getMessage());

        verify(repository).findById(domainIn.getId());
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    // -------------------------
    // deleteUser
    // -------------------------

    @Test
    void deleteUser_shouldDeactivateUser_whenUserFoundActive() {
        Long id = 10L;

        UserJpa existingJpa = mock(UserJpa.class);

        when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existingJpa));


        userRepository.deleteUser(id);

        verify(repository).findByIdAndActiveTrue(id);
        verify(existingJpa).deactivate();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteUser_shouldThrow_whenUserNotFoundBecauseFindGet() {
        Long id = 10L;
        when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userRepository.deleteUser(id));

        verify(repository).findByIdAndActiveTrue(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }
}
