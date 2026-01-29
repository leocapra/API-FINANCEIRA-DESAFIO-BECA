package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailsUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private DetailsUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DetailsUserUseCase(userRepository);
    }

    @Test
    void shouldReturnUser_whenUserExistsAndActive() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-11111111111");

        User user = new User(
                id,
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "senha",
                "+55 11 91234-5678"
        );

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(user));

        Optional<User> result = useCase.execute(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Leonardo", result.get().getNome());

        verify(userRepository).findByIdAndActiveTrue(id);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldReturnEmptyOptional_whenUserNotFoundOrInactive() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-11111111111");

        when(userRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        Optional<User> result = useCase.execute(id);

        assertTrue(result.isEmpty());

        verify(userRepository).findByIdAndActiveTrue(id);
        verifyNoMoreInteractions(userRepository);
    }
}
