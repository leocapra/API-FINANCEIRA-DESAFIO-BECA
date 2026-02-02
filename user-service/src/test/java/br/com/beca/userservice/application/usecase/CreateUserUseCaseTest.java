package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.PasswordHasher;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.RequestUserData;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Cobertura total para CreateUserUseCase.execute:
 *  - Campos obrigatórios vazios/nulos
 *  - Falhas de validação (CPF/Email/Telefone) conforme RegexpValidation real
 *  - CPF existente (reativa se inativo, lança se ativo)
 *  - Email existente (reativa se inativo, lança se ativo)
 *  - Criação de novo usuário (email lower-case, hash de senha, conta bancária)
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private BankAccountPort bankAccount;

    // Mocks do agregado User para diferentes papéis
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private User userFactory;  // record field 'user' usado como "factory"
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private User existingUser;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private User updatedUser;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private User newUser;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private User createdUser;

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUserUseCase(userRepository, passwordHasher, userFactory, bankAccount);
    }

    // ---------- Helpers ----------
    private RequestUserData validDtoUpperEmail() {
        // Constrói DTO com TODOS os campos atendendo ao regex fornecido
        // Email propositalmente em MAIÚSCULAS para validar conversão para lower-case no fluxo de criação
        return new RequestUserData(
                "123.456.789-01",
                "Leonardo",
                "LEO@MAIL.COM",
                "Senha!123",
                "+55 11 91234-5678"
        );
    }

    // ---------- Campos vazios/nulos ----------
    @Test
    void deveLancar_FieldIsEmpty_quandoCpfVazio() {
        RequestUserData dto = new RequestUserData("  ", "Nome", "a@b.com", "s", "+55 11 91234-5678");
        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("cpf"));
    }

    @Test
    void deveLancar_FieldIsEmpty_quandoNomeNulo() {
        RequestUserData dto = new RequestUserData("123.456.789-01", null, "a@b.com", "s", "+55 11 91234-5678");
        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("nome"));
    }

    @Test
    void deveLancar_FieldIsEmpty_quandoEmailVazio() {
        RequestUserData dto = new RequestUserData("123.456.789-01", "Nome", "   ", "s", "+55 11 91234-5678");
        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    void deveLancar_FieldIsEmpty_quandoSenhaNula() {
        RequestUserData dto = new RequestUserData("123.456.789-01", "Nome", "a@b.com", null, "+55 11 91234-5678");
        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("senha"));
    }

    @Test
    void deveLancar_FieldIsEmpty_quandoTelefoneVazio() {
        RequestUserData dto = new RequestUserData("123.456.789-01", "Nome", "a@b.com", "s", " ");
        FieldIsEmptyException ex = assertThrows(FieldIsEmptyException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("telefone"));
    }

    // ---------- Regex inválidos (usando o regex real) ----------
    @Test
    void deveLancar_Regexp_quandoCpfInvalido() {
        // CPF inválido (sem pontos e hífen conforme regex: ^\d{3}.\d{3}.\d{3}-\d{2}$)
        RequestUserData dto = new RequestUserData("12345678901", "Nome", "a@b.com", "Senha!123", "+55 11 91234-5678");
        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("CPF 12345678901"));
    }

    @Test
    void deveLancar_Regexp_quandoEmailInvalido() {
        // Email inválido (faltando '@' por exemplo)
        RequestUserData dto = new RequestUserData("123.456.789-01", "Nome", "email-invalido", "Senha!123", "+55 11 91234-5678");
        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("Email email-invalido"));
    }

    @Test
    void deveLancar_Regexp_quandoTelefoneInvalido() {
        // Telefone inválido (faltando +código país e espaços conforme ^\+\d{1,3}\s\d{2}\s9\d{4}-\d{4}$)
        RequestUserData dto = new RequestUserData("123.456.789-01", "Nome", "a@b.com", "Senha!123", "1191234-5678");
        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("Telefone 1191234-5678"));
    }

    // ---------- Fluxo: CPF já cadastrado ----------
    @Test
    void deveReativarUsuario_quandoCpfEncontradoEUsuarioInativo() throws Exception, FieldIsEmptyException, RegexpException {
        RequestUserData dto = validDtoUpperEmail();
        String emailLower = dto.email().toLowerCase();

        when(userRepository.findByCpf(dto.cpf())).thenReturn(Optional.of(existingUser));
        when(existingUser.isActive()).thenReturn(false);
        when(existingUser.getEmail()).thenReturn(emailLower);
        when(userRepository.updateUser(existingUser)).thenReturn(updatedUser);

        User result = useCase.execute(dto);

        assertSame(updatedUser, result);
        verify(existingUser).activate();
        verify(bankAccount).createAccountForUser(anyString(), eq(emailLower));
        verify(userRepository).updateUser(existingUser);

        // Não deve tentar criar novo usuário, nem buscar por email (retorno antecipado)
        verify(userRepository, never()).saveUser(any());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void deveLancar_AlreadyExists_quandoCpfEncontradoEUsuarioAtivo() {
        RequestUserData dto = validDtoUpperEmail();

        when(userRepository.findByCpf(dto.cpf())).thenReturn(Optional.of(existingUser));
        when(existingUser.isActive()).thenReturn(true);

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("CPF " + dto.cpf()));
        verify(userRepository, never()).updateUser(any());
        verify(bankAccount, never()).createAccountForUser(anyString(), anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).saveUser(any());
    }

    // ---------- Fluxo: Email já cadastrado ----------
    @Test
    void deveReativarUsuario_quandoEmailEncontradoEUsuarioInativo() throws Exception, FieldIsEmptyException, RegexpException {
        RequestUserData dto = validDtoUpperEmail();
        String emailLower = dto.email().toLowerCase();

        when(userRepository.findByCpf(dto.cpf())).thenReturn(Optional.empty());
        // Importante: o código consulta findByEmail(dto.email()) ANTES de lower-case
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));

        when(existingUser.isActive()).thenReturn(false);
        when(existingUser.getEmail()).thenReturn(emailLower);
        when(userRepository.updateUser(existingUser)).thenReturn(updatedUser);

        User result = useCase.execute(dto);

        assertSame(updatedUser, result);
        verify(existingUser).activate();
        verify(bankAccount).createAccountForUser(anyString(), eq(emailLower));
        verify(userRepository).updateUser(existingUser);
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void deveLancar_AlreadyExists_quandoEmailEncontradoEUsuarioAtivo() {
        RequestUserData dto = validDtoUpperEmail();

        when(userRepository.findByCpf(dto.cpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(existingUser));
        when(existingUser.isActive()).thenReturn(true);

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class, () -> useCase.execute(dto));
        assertTrue(ex.getMessage().contains("Email " + dto.email()));
        verify(userRepository, never()).updateUser(any());
        verify(bankAccount, never()).createAccountForUser(anyString(), anyString());
        verify(userRepository, never()).saveUser(any());
    }

    // ---------- Fluxo: Criação de novo usuário ----------
    @Test
    void deveCriarNovoUsuario_comEmailLowerCase_hashSenha_eContaBancaria() throws Exception, FieldIsEmptyException, RegexpException {
        RequestUserData dto = validDtoUpperEmail();
        String emailLower = dto.email().toLowerCase();

        when(userRepository.findByCpf(dto.cpf())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        // Captura o email passado ao factory para garantir conversão para lower-case
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);

        when(userFactory.createUser(
                eq(dto.cpf()),
                eq(dto.nome()),
                emailCaptor.capture(),
                eq(dto.senha()),
                eq(dto.telefone())
        )).thenReturn(newUser);

        when(userRepository.saveUser(newUser)).thenReturn(createdUser);
        when(createdUser.getEmail()).thenReturn(emailLower);

        User result = useCase.execute(dto);

        assertSame(createdUser, result);
        assertEquals(emailLower, emailCaptor.getValue(), "Email deve ser convertido para lower-case na criação");
        verify(newUser).ensurePasswordHashed(passwordHasher);
        verify(userRepository).saveUser(newUser);
        verify(bankAccount).createAccountForUser(anyString(), eq(emailLower));

        // Não deve chamar updateUser nesse fluxo
        verify(userRepository, never()).updateUser(any());
    }
}