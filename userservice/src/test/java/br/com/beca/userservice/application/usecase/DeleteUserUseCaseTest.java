package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private DeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserUseCase(userRepository);
    }

    @Test
    void shouldThrowNotFoundException_whenUserNotFoundOrNotActive() {
        Long id = 10L;

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> useCase.execute(id));
        assertEquals("Não foi possível encontrar usuário com id " + id + " para exclusão!", ex.getMessage());

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository, never()).deleteUser(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldDeleteUser_whenUserExistsAndActive() {
        Long id = 10L;
        User activeUser = new User(id, "123.456.789-09", "Leo", "leo@email.com", "x", "+55 11 91234-5678");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(activeUser));

        assertDoesNotThrow(() -> useCase.execute(id));

        verify(userRepository).findByIdAndActiveTrue(id);
        verify(userRepository).deleteUser(id);
        verifyNoMoreInteractions(userRepository);
    }
}
