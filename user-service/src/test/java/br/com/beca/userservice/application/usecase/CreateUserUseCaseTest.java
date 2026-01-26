package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.domain.PasswordHasher;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUserUseCase(userRepository, passwordHasher);
    }

    private User validUserRawPassword() {
        return new User(
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "Senha@123",
                "+55 11 91234-5678"
        );
    }

    // -------------------------
    // Campos obrigatórios
    // -------------------------

    @Test
    void shouldThrowFieldIsEmptyException_whenCpfIsNull() {
        User user = new User(null, "Leonardo", "leo@email.com", "Senha@123", "+55 11 91234-5678");

        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(user));
        assertEquals("Campo cpf não pode ser vazio!", ex.getMessage());

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldThrowFieldIsEmptyException_whenNomeIsBlank() {
        User user = new User("123.456.789-09", " ", "leo@email.com", "Senha@123", "+55 11 91234-5678");

        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(user));
        assertEquals("Campo nome não pode ser vazio!", ex.getMessage());

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldThrowFieldIsEmptyException_whenEmailIsBlank() {
        User user = new User("123.456.789-09", "Leonardo", " ", "Senha@123", "+55 11 91234-5678");

        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(user));
        assertEquals("Campo email não pode ser vazio!", ex.getMessage());

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldThrowFieldIsEmptyException_whenSenhaIsBlank() {
        User user = new User("123.456.789-09", "Leonardo", "leo@email.com", " ", "+55 11 91234-5678");

        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(user));
        assertEquals("Campo senha não pode ser vazio!", ex.getMessage());

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldThrowFieldIsEmptyException_whenTelefoneIsBlank() {
        User user = new User("123.456.789-09", "Leonardo", "leo@email.com", "Senha@123", " ");

        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(user));
        assertEquals("Campo telefone não pode ser vazio!", ex.getMessage());

        verifyNoInteractions(userRepository, passwordHasher);
    }

    // -------------------------
    // CPF existente
    // -------------------------

    @Test
    void shouldThrowAlreadyExistsException_whenCpfExistsAndActive() {
        User input = validUserRawPassword();
        User existing = new User(UUID.fromString("11111111-1111-1111-1111-11111111111"), input.getCpf(), "Outro", "outro@email.com", "x", "+55 11 91234-5678");

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class, () -> useCase.execute(input));
        assertTrue(ex.getMessage().contains("CPF " + input.getCpf()));

        verify(userRepository, never()).saveUser(any());
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void shouldReactivateAndSave_whenCpfExistsAndInactive() throws FieldIsEmptyException, RegexpException {
        User input = validUserRawPassword();
        User existing = new User(UUID.fromString("11111111-1111-1111-1111-11111111111"), input.getCpf(), "Nome Antigo", "antigo@email.com", "x", "+55 11 91234-5678");
        existing.deactivate();

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        // como o use case salva o INPUT, stub tem que ser pro input (ou any)
        when(passwordHasher.isHashed(anyString())).thenReturn(true);
        when(userRepository.saveUser(input)).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(input);

        assertTrue(existing.isActive(), "Deveria reativar o usuário existente encontrado");
        assertSame(input, saved, "O use case salva/retorna o input (não o existing)");

        verify(userRepository).saveUser(input);
        verify(passwordHasher).isHashed(anyString());
        verify(passwordHasher, never()).hash(anyString());
    }

    // -------------------------
    // Email existente
    // -------------------------

    @Test
    void shouldThrowAlreadyExistsException_whenEmailExistsAndActive() {
        User input = validUserRawPassword();
        User existing = new User(UUID.fromString("11111111-1111-1111-1111-11111111111"), "999.999.999-99", "Outro", input.getEmail(), "x", "+55 11 91234-5678");

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existing));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class, () -> useCase.execute(input));
        assertTrue(ex.getMessage().contains("Email " + input.getEmail()));

        verify(userRepository, never()).saveUser(any());
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void shouldReactivateAndSave_whenEmailExistsAndInactive() throws FieldIsEmptyException, RegexpException {
        User input = validUserRawPassword();
        User existing = new User(UUID.fromString("11111111-1111-1111-1111-11111111111"), "999.999.999-99", "Outro", input.getEmail(), "x", "+55 11 91234-5678");
        existing.deactivate();

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existing));

        // como o use case salva o INPUT, stub tem que ser pro input (ou any)
        when(passwordHasher.isHashed(anyString())).thenReturn(true);
        when(userRepository.saveUser(input)).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(input);

        assertTrue(existing.isActive(), "Deveria reativar o usuário existente encontrado");
        assertSame(input, saved, "O use case salva/retorna o input (não o existing)");

        verify(userRepository).saveUser(input);
        verify(passwordHasher).isHashed(anyString());
        verify(passwordHasher, never()).hash(anyString());
    }

    // -------------------------
    // Regex inválidos reais
    // -------------------------

    @Test
    void shouldThrowRegexpException_whenCpfInvalid() {
        User input = new User(
                "12345678909",
                "Leonardo",
                "leo@email.com",
                "Senha@123",
                "+55 11 91234-5678"
        );

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(input));
        assertTrue(ex.getMessage().contains("CPF " + input.getCpf()));

        verify(userRepository, never()).saveUser(any());
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void shouldThrowRegexpException_whenEmailInvalid() {
        User input = new User(
                "123.456.789-09",
                "Leonardo",
                "leo-email.com",
                "Senha@123",
                "+55 11 91234-5678"
        );

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(input));
        assertTrue(ex.getMessage().contains("Email " + input.getEmail()));

        verify(userRepository, never()).saveUser(any());
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void shouldThrowRegexpException_whenTelefoneInvalid() {
        User input = new User(
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "Senha@123",
                "11 91234-5678"
        );

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(input));
        assertTrue(ex.getMessage().contains("Telefone " + input.getTelefone()));

        verify(userRepository, never()).saveUser(any());
        verifyNoInteractions(passwordHasher);
    }

    // -------------------------
    // Hash de senha
    // -------------------------

    @Test
    void shouldHashPassword_whenNotHashed() throws FieldIsEmptyException, RegexpException {
        User input = validUserRawPassword();

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        when(passwordHasher.isHashed("Senha@123")).thenReturn(false);
        when(passwordHasher.hash("Senha@123")).thenReturn("$2a$12$HASH");

        when(userRepository.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(input);

        assertEquals("$2a$12$HASH", saved.getSenha());
        verify(passwordHasher).hash("Senha@123");
    }

    @Test
    void shouldNotHashPassword_whenAlreadyHashed() throws FieldIsEmptyException, RegexpException {
        User input = new User(
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "$2a$12$6ZsbdL5g2dj9q.uI8wceUOz9tylxcFEYg9RKXbZqM1BcSxXqG0QZq",
                "+55 11 91234-5678"
        );

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        when(passwordHasher.isHashed(input.getSenha())).thenReturn(true);
        when(userRepository.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(input);

        assertEquals(input.getSenha(), saved.getSenha());
        verify(passwordHasher, never()).hash(anyString());
    }

    // -------------------------
    // Caminho feliz
    // -------------------------

    @Test
    void shouldSaveUser_whenAllValid() throws FieldIsEmptyException, RegexpException {
        User input = validUserRawPassword();

        when(userRepository.findByCpf(input.getCpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        when(passwordHasher.isHashed(input.getSenha())).thenReturn(false);
        when(passwordHasher.hash(input.getSenha())).thenReturn("$2a$12$HASH");

        when(userRepository.saveUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = useCase.execute(input);

        assertNotNull(saved);
        assertEquals("$2a$12$HASH", saved.getSenha());
        verify(userRepository).saveUser(saved);
    }
}
