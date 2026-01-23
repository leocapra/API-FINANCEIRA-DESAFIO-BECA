package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.repository.UserRepository;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PageDataDomain;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private ListUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUsersUseCase(userRepository);
    }

    @Test
    void shouldReturnPaginatedResponseFromRepository() {
        PageDataDomain pageData = mock(PageDataDomain.class);
        @SuppressWarnings("unchecked")
        PaginatedResponse<User> expected = (PaginatedResponse<User>) mock(PaginatedResponse.class);

        when(userRepository.findAllActive(pageData)).thenReturn(expected);

        PaginatedResponse<User> result = useCase.execute(pageData);

        assertSame(expected, result);

        verify(userRepository).findAllActive(pageData);
        verifyNoMoreInteractions(userRepository);
    }
}
