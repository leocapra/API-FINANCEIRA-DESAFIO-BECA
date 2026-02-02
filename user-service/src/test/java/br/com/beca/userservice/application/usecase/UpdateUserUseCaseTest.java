package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.dto.UpdateUserData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.Roles;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountPort bankAccountPort;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    private UUID userId;
    private User userMock;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Simulando o usuário que já existe no banco
        userMock = new User(
                userId, "123.456.789-00", Roles.ROLE_USER,
                "João Silva", "joao@email.com", "senha123",
                "+55 11 98888-8888", true, null, null
        );
    }

    @Test
    @DisplayName("Deve atualizar e-mail no banco e no usuário quando os dados forem válidos")
    void execute_ShouldUpdateUserAndNotifyBankAccount_WhenDataIsValid() throws RegexpException {
        // GIVEN
        var dto = new UpdateUserData("João Alterado", "novo@email.com", "+55 11 91234-5678");
        var token = new TokenInfoData(userId.toString(), "ROLE_USER");

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(userMock));
        when(userRepository.updateUser(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        User result = updateUserUseCase.execute(userId, dto, token);

        // THEN
        assertEquals("João Alterado", result.getNome());
        assertEquals("novo@email.com", result.getEmail());
        verify(bankAccountPort).updateAccountEmail(userId.toString(), "novo@email.com");
        verify(userRepository).updateUser(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção de permissão quando um ROLE_USER tenta editar outro usuário")
    void execute_ShouldThrowPermissionException_WhenUserEditsAnotherUser() {
        // GIVEN
        var dto = new UpdateUserData("Nome", "email@test.com", null);
        var token = new TokenInfoData(UUID.randomUUID().toString(), "ROLE_USER"); // ID diferente do solicitado

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(userMock));

        // WHEN & THEN
        assertThrows(PermissionException.class, () -> updateUserUseCase.execute(userId, dto, token));
    }

    @Test
    @DisplayName("Deve lançar exceção de Regex quando o telefone for inválido")
    void execute_ShouldThrowRegexpException_WhenPhoneIsInvalid() {
        // GIVEN
        var dto = new UpdateUserData("Nome", "email@correto.com", "1199999999"); // Formato fora do padrão
        var token = new TokenInfoData(userId.toString(), "ROLE_USER");

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(userMock));

        // WHEN & THEN
        RegexpException ex = assertThrows(RegexpException.class, () -> updateUserUseCase.execute(userId, dto, token));
        assertTrue(ex.getMessage().contains("Telefone"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando ROLE_ADMIN tenta editar a si mesmo")
    void execute_ShouldThrowPermissionException_WhenAdminEditsThemselves() {
        // GIVEN
        var dto = new UpdateUserData("Admin", "admin@email.com", null);
        var token = new TokenInfoData(userId.toString(), "ROLE_ADMIN");

        when(userRepository.findByIdAndActiveTrue(userId)).thenReturn(Optional.of(userMock));

        // WHEN & THEN
        PermissionException ex = assertThrows(PermissionException.class, () -> updateUserUseCase.execute(userId, dto, token));
        assertEquals("Administrador não pode ser editado!", ex.getMessage());
    }
}