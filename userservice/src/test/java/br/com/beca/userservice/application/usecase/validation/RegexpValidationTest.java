package br.com.beca.userservice.application.usecase.validation;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.application.usecase.UpdateUserUseCase;
import br.com.beca.userservice.domain.dto.UpdateUserData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UpdateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserUseCase(userRepository);
    }

    private User existingUser(Long id) {
        return new User(
                id,
                "123.456.789-09",
                "Nome Antigo",
                "old@email.com",
                "senhaHash",
                "+55 11 91234-5678",
                true
        );
    }

    // -------------------------
    // Usuário não encontrado
    // -------------------------

    @Test
    void shouldThrowNotFoundException_whenUserDoesNotExist() {
        Long id = 1L;
        UpdateUserData dto = new UpdateUserData("Novo Nome", "novo@email.com", "+55 11 91234-5678");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> useCase.execute(id, dto));
        assertEquals("Usuário não encontrado", ex.getMessage());

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).updateUser(any());
    }

    // -------------------------
    // Email inválido
    // -------------------------

    @Test
    void shouldThrowRegexpException_whenEmailInvalid() {
        Long id = 1L;
        UpdateUserData dto = new UpdateUserData("Novo Nome", "email-invalido", "+55 11 91234-5678");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existingUser(id)));

        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(id, dto));
        assertEquals("Email não tem um formato válido!", ex.getMessage());

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).updateUser(any());
    }

    // -------------------------
    // Telefone inválido
    // -------------------------

    @Test
    void shouldThrowRegexpException_whenTelefoneInvalid() {
        Long id = 1L;
        UpdateUserData dto = new UpdateUserData("Novo Nome", "novo@email.com", "11 91234-5678");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existingUser(id)));

        RegexpException ex = assertThrows(RegexpException.class, () -> useCase.execute(id, dto));
        assertEquals("Telefone não tem um formato válido!", ex.getMessage());

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).updateUser(any());
    }

    // -------------------------
    // Atualização válida
    // -------------------------

    @Test
    void shouldUpdateUser_whenDtoValid() throws RegexpException {
        Long id = 1L;
        User existing = existingUser(id);

        UpdateUserData dto = new UpdateUserData(
                "Novo Nome",
                "novo@email.com",
                "+55 11 91234-5678"
        );

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existing));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.updateUser(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        User updated = useCase.execute(id, dto);

        User sentToRepo = captor.getValue();

        assertNotNull(updated);
        assertEquals(id, sentToRepo.getId());
        assertEquals("123.456.789-09", sentToRepo.getCpf());
        assertEquals("Novo Nome", sentToRepo.getNome());
        assertEquals("novo@email.com", sentToRepo.getEmail());
        assertEquals("+55 11 91234-5678", sentToRepo.getTelefone());

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository).updateUser(any(User.class));
    }

    // -------------------------
    // Campos vazios NÃO alteram dados
    // -------------------------

    @Test
    void shouldNotChangeFields_whenDtoBlankOrNull() throws RegexpException {
        Long id = 1L;
        User existing = existingUser(id);

        UpdateUserData dto = new UpdateUserData(" ", " ", null);

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(existing));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.updateUser(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(id, dto);

        User sentToRepo = captor.getValue();

        assertEquals("Nome Antigo", sentToRepo.getNome());
        assertEquals("old@email.com", sentToRepo.getEmail());
        assertEquals("+55 11 91234-5678", sentToRepo.getTelefone());

        verify(userRepository).updateUser(any(User.class));
    }
}
