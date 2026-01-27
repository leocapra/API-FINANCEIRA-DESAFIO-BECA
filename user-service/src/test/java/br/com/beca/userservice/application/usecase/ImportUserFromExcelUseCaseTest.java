package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.domain.dto.ImportResponseData;
import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportUserFromExcelUseCaseTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    private ImportUserFromExcelUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ImportUserFromExcelUseCase(createUserUseCase);
    }

    private ImportUserRequestData validDto() {
        return new ImportUserRequestData(
                "123.456.789-09",
                "Leonardo",
                "leo@email.com",
                "Senha@123",
                "+55 11 91234-5678"
        );
    }

    // -------------------------
    // Todos sucesso
    // -------------------------

    @Test
    void shouldImportAllUsersSuccessfully() throws Exception, FieldIsEmptyException, RegexpException {
        List<ImportUserRequestData> dtos = List.of(
                validDto(),
                validDto(),
                validDto()
        );

        when(createUserUseCase.execute(any(User.class))).thenReturn(new User());

        ImportResponseData response = useCase.execute(dtos);

        assertEquals(3, response.totalRows());
        assertEquals(3, response.imported());
        assertEquals(0, response.failed());

        verify(createUserUseCase, times(3)).execute(any(User.class));
    }

    // -------------------------
    // Sucesso parcial
    // -------------------------

    @Test
    void shouldCountFailed_whenSomeUsersFail() throws Exception, FieldIsEmptyException, RegexpException {
        List<ImportUserRequestData> dtos = List.of(
                validDto(),
                validDto(),
                validDto()
        );

        when(createUserUseCase.execute(any(User.class)))
                .thenReturn(new User())   // 1º sucesso
                .thenThrow(new RuntimeException()) // 2º falha
                .thenReturn(new User()); // 3º sucesso

        ImportResponseData response = useCase.execute(dtos);

        assertEquals(3, response.totalRows());
        assertEquals(2, response.imported());
        assertEquals(1, response.failed());
    }

    // -------------------------
    // Todos falham
    // -------------------------

    @Test
    void shouldCountAllFailed_whenAllUsersFail() throws Exception, FieldIsEmptyException, RegexpException {
        List<ImportUserRequestData> dtos = List.of(
                validDto(),
                validDto()
        );

        when(createUserUseCase.execute(any(User.class)))
                .thenThrow(new RuntimeException());

        ImportResponseData response = useCase.execute(dtos);

        assertEquals(2, response.totalRows());
        assertEquals(0, response.imported());
        assertEquals(2, response.failed());
    }

    // -------------------------
    // Lista vazia
    // -------------------------

    @Test
    void shouldReturnZeroCounts_whenListIsEmpty() {
        ImportResponseData response = useCase.execute(List.of());

        assertEquals(0, response.totalRows());
        assertEquals(0, response.imported());
        assertEquals(0, response.failed());

        verifyNoInteractions(createUserUseCase);
    }

    // -------------------------
    // Garante criação do User
    // -------------------------

    @Test
    void shouldMapDtoToUserCorrectly() throws FieldIsEmptyException, RegexpException {
        ImportUserRequestData dto = validDto();

        when(createUserUseCase.execute(any(User.class))).thenReturn(new User());

        useCase.execute(List.of(dto));

        verify(createUserUseCase).execute(argThat(user ->
                user.getCpf().equals(dto.cpf()) &&
                        user.getNome().equals(dto.nome()) &&
                        user.getEmail().equals(dto.email()) &&
                        user.getSenha().equals(dto.senha()) &&
                        user.getTelefone().equals(dto.telefone())
        ));
    }
}
