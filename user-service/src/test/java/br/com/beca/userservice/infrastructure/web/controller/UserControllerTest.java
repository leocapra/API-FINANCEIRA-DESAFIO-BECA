package br.com.beca.userservice.infrastructure.web.controller;

import br.com.beca.userservice.application.usecase.*;
import br.com.beca.userservice.domain.dto.*;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import br.com.beca.userservice.infrastructure.gateway.ExcelUserImportMapper;
import br.com.beca.userservice.infrastructure.security.SecurityFilter;
import br.com.beca.userservice.infrastructure.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.http.converters.preferred-json-mapper=jackson2")
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean CreateUserUseCase createUserUseCase;
    @MockitoBean ListUsersUseCase listUsersUseCase;
    @MockitoBean DetailsUserUseCase detailsUserUseCase;
    @MockitoBean UpdateUserUseCase updateUserUseCase;
    @MockitoBean DeleteUserUseCase deleteUserUseCase;
    @MockitoBean ImportUserFromExcelUseCase importUserFromExcelUseCase;
    @MockitoBean ExcelUserImportMapper excelUserImportMapper;

    @MockitoBean TokenService tokenService;
    @MockitoBean SecurityFilter securityFilter;

    @Test
    @DisplayName("POST /usuarios deve criar usuário e retornar 201")
    void create_shouldReturn201() throws Exception, FieldIsEmptyException, RegexpException {
        UUID id = UUID.randomUUID();

        RequestUserData request = new RequestUserData(
                "123.456.789-10",
                "Leonardo",
                "leo@gmail.com",
                "Senha@123",
                "+55 11 91234-5678"
        );

        User created = mock(User.class);
        when(created.getId()).thenReturn(id);
        when(createUserUseCase.execute(any(User.class))).thenReturn(created);

        User details = mock(User.class);
        when(details.getId()).thenReturn(id);
        when(details.getCpf()).thenReturn("123.456.789-10");
        when(details.getNome()).thenReturn("Leonardo");
        when(details.getEmail()).thenReturn("leo@gmail.com");
        when(details.getTelefone()).thenReturn("+55 11 91234-5678");
        when(detailsUserUseCase.execute(eq(id))).thenReturn(Optional.of(details));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/usuarios/" + id)))
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.nome", is("Leonardo")))
                .andExpect(jsonPath("$.email", is("leo@gmail.com")));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(createUserUseCase).execute(captor.capture());
        verify(detailsUserUseCase).execute(id);
    }

    @Test
    @DisplayName("GET /usuarios deve retornar 200 com PaginatedResponse")
    void listUsers_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getCpf()).thenReturn("123.456.789-10");
        when(u.getNome()).thenReturn("Leonardo");
        when(u.getEmail()).thenReturn("leo@gmail.com");
        when(u.getTelefone()).thenReturn("+55 11 91234-5678");

        PaginatedResponse<User> domainPage = new PaginatedResponse<>(
                List.of(u),
                0,
                1,
                1L,
                10,
                false,
                false
        );

        when(listUsersUseCase.execute(any())).thenReturn(domainPage);

        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(listUsersUseCase).execute(any());
    }

    @Test
    @DisplayName("GET /usuarios/{id} deve retornar 200")
    void detailUser_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        User details = mock(User.class);
        when(details.getId()).thenReturn(id);
        when(details.getCpf()).thenReturn("123.456.789-10");
        when(details.getNome()).thenReturn("Leonardo");
        when(details.getEmail()).thenReturn("leo@gmail.com");
        when(details.getTelefone()).thenReturn("+55 11 91234-5678");

        when(detailsUserUseCase.execute(eq(id))).thenReturn(Optional.of(details));

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.cpf", is("123.456.789-10")))
                .andExpect(jsonPath("$.nome", is("Leonardo")))
                .andExpect(jsonPath("$.email", is("leo@gmail.com")))
                .andExpect(jsonPath("$.telefone", is("+55 11 91234-5678")));

        verify(detailsUserUseCase).execute(id);
    }

    @Test
    @DisplayName("PUT /usuarios/{id} deve atualizar e retornar 200")
    void updateUser_shouldReturn200() throws Exception, RegexpException {
        UUID id = UUID.randomUUID();

        UpdateUserData update = new UpdateUserData(
                "Novo Nome",
                "novo@gmail.com",
                "+55 11 91234-5678"
        );

        User updated = mock(User.class);
        when(updated.getId()).thenReturn(id);

        doReturn(updated)
                .when(updateUserUseCase)
                .execute(eq(id), any(UpdateUserData.class));

        User details = mock(User.class);
        when(details.getId()).thenReturn(id);
        when(details.getCpf()).thenReturn("123.456.789-10");
        when(details.getNome()).thenReturn("Novo Nome");
        when(details.getEmail()).thenReturn("novo@gmail.com");
        when(details.getTelefone()).thenReturn("+55 11 91234-5678");

        when(detailsUserUseCase.execute(eq(id))).thenReturn(Optional.of(details));

        mockMvc.perform(put("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value("novo@gmail.com"))
                .andExpect(jsonPath("$.telefone").value("+55 11 91234-5678"));

        verify(updateUserUseCase).execute(eq(id), any(UpdateUserData.class));
        verify(detailsUserUseCase).execute(id);
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} deve retornar 200")
    void deleteUser_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(deleteUserUseCase).execute(eq(id));

        mockMvc.perform(delete("/usuarios/{id}", id))
                .andExpect(status().isOk());

        verify(deleteUserUseCase).execute(id);
    }

    @Test
    @DisplayName("POST /usuarios/importar deve importar Excel e retornar resultado")
    void importUsers_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "usuarios.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake".getBytes()
        );

        List<ImportUserRequestData> parsed = List.of(
                new ImportUserRequestData(
                        "Leonardo",
                        "123.456.789-10",
                        "leo@gmail.com",
                        "$2a$12$hash...",
                        "+55 11 91234-5678"
                )
        );

        ImportResponseData response = new ImportResponseData(1, 1, 0);

        when(excelUserImportMapper.map(any())).thenReturn(parsed);
        when(importUserFromExcelUseCase.execute(eq(parsed))).thenReturn(response);

        mockMvc.perform(multipart("/usuarios/importar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows", is(1)))
                .andExpect(jsonPath("$.imported", is(1)))
                .andExpect(jsonPath("$.failed", is(0)));

        verify(excelUserImportMapper).map(any());
        verify(importUserFromExcelUseCase).execute(parsed);
    }
}
