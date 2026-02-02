package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.BankAccountPort;
import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Cobertura total para DeleteUserUseCase.execute:
 *  - NotFound quando usuário ativo não existe
 *  - ADMIN não pode se auto-excluir
 *  - ADMIN pode excluir outro usuário
 *  - USER pode excluir a si mesmo
 *  - USER não pode excluir outro usuário
 *  - Verifica chamadas a deleteUser e deleteAccountById quando aplicável
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private BankAccountPort bankAccountPort;

    // User retornado pelo repositório; RETURNS_DEEP_STUBS permite getId().toString() sem NPE
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private User foundUser;

    private DeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserUseCase(userRepository, bankAccountPort);
    }

    @Test
    void deveLancar_NotFound_quandoUsuarioAtivoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(id.toString(), "ROLE_ADMIN");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> useCase.execute(id, token));
        assertTrue(ex.getMessage().contains("id " + id));
        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).deleteUser(any());
        verify(bankAccountPort, never()).deleteAccountById(anyString());
    }

    @Test
    void admin_naoPodeSeAutoExcluir_lancaPermissionException() {
        UUID id = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(id.toString(), "ROLE_ADMIN");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(foundUser));
        when(foundUser.getId()).thenReturn(id); // usado para comparar com token.userId()

        PermissionException ex = assertThrows(PermissionException.class, () -> useCase.execute(id, token));
        assertTrue(ex.getMessage().toLowerCase().contains("administrador"));
        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).deleteUser(any());
        verify(bankAccountPort, never()).deleteAccountById(anyString());
    }

    @Test
    void admin_podeExcluirOutroUsuario() {
        UUID idDoAlvo = UUID.randomUUID();
        // Token de admin com outro userId diferente do alvo
        TokenInfoData token = new TokenInfoData(UUID.randomUUID().toString(), "ROLE_ADMIN");

        when(userRepository.findByIdAndActiveTrue(idDoAlvo)).thenReturn(Optional.of(foundUser));
        when(foundUser.getId()).thenReturn(idDoAlvo);

        // Act
        assertDoesNotThrow(() -> useCase.execute(idDoAlvo, token));

        // Assert
        verify(userRepository).findByIdAndActiveTrue(idDoAlvo);
        verify(userRepository).deleteUser(eq(idDoAlvo));
        verify(bankAccountPort).deleteAccountById(eq(idDoAlvo.toString()));
    }

    @Test
    void user_podeExcluirASiMesmo() {
        UUID id = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(id.toString(), "ROLE_USER");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(foundUser));
        when(foundUser.getId()).thenReturn(id);

        assertDoesNotThrow(() -> useCase.execute(id, token));
        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository).deleteUser(eq(id));
        verify(bankAccountPort).deleteAccountById(eq(id.toString()));
    }

    @Test
    void user_naoPodeExcluirOutroUsuario_lancaPermissionException() {
        UUID idDoAlvo = UUID.randomUUID();
        UUID idDoToken = UUID.randomUUID(); // diferente do alvo
        TokenInfoData token = new TokenInfoData(idDoToken.toString(), "ROLE_USER");

        when(userRepository.findByIdAndActiveTrue(idDoAlvo)).thenReturn(Optional.of(foundUser));
        when(foundUser.getId()).thenReturn(idDoAlvo);

        PermissionException ex = assertThrows(PermissionException.class, () -> useCase.execute(idDoAlvo, token));
        assertTrue(ex.getMessage().toLowerCase().contains("permissão"));
        verify(userRepository).findByIdAndActiveTrue(idDoAlvo);
        verify(userRepository, never()).deleteUser(any());
        verify(bankAccountPort, never()).deleteAccountById(anyString());
    }
}