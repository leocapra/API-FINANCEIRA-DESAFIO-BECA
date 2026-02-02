package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.domain.dto.ImportErrorDetails;
import br.com.beca.userservice.domain.dto.ImportResponseData;
import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import br.com.beca.userservice.domain.dto.RequestUserData;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes para ImportUserFromExcelUseCase:
 *  - lista nula
 *  - lista vazia
 *  - todos com sucesso
 *  - exceções conhecidas (AlreadyExists, FieldIsEmpty, Regexp)
 *  - exceção genérica
 *  - elemento nulo na lista
 *  - ordem dos erros preservada
 */
@ExtendWith(MockitoExtension.class)
class ImportUserFromExcelUseCaseTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    // Não é usado pelo use case; mantido aqui apenas se seu construtor anterior ainda exigia.
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private User userFactory;

    private ImportUserFromExcelUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ImportUserFromExcelUseCase(createUserUseCase);
    }

    // Helper para montar dados válidos para importação
    private ImportUserRequestData dto(String cpf) {
        return new ImportUserRequestData(
                "Nome " + cpf,
                cpf,
                "user" + cpf.replaceAll("\\D", "") + "@mail.com",
                "Senha!123",
                "+55 11 91234-5678"
        );
    }

    @Test
    void deveRetornarZerado_quandoListaForNula() {
        ImportResponseData resp = useCase.execute(null);

        assertEquals(0, resp.totalRows());
        assertEquals(0, resp.imported());
        assertEquals(0, resp.failed());
        assertNotNull(resp.errors());
        assertTrue(resp.errors().isEmpty());
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void deveRetornarZerado_quandoListaForVazia() {
        ImportResponseData resp = useCase.execute(List.of());

        assertEquals(0, resp.totalRows());
        assertEquals(0, resp.imported());
        assertEquals(0, resp.failed());
        assertNotNull(resp.errors());
        assertTrue(resp.errors().isEmpty());
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void deveImportarTodosComSucesso() throws Exception, FieldIsEmptyException, RegexpException {
        List<ImportUserRequestData> dados = List.of(
                dto("123.456.789-01"),
                dto("987.654.321-00"),
                dto("111.222.333-44")
        );

        when(createUserUseCase.execute(any(RequestUserData.class))).thenReturn(mock(User.class));

        ImportResponseData resp = useCase.execute(dados);

        assertEquals(3, resp.totalRows());
        assertEquals(3, resp.imported());
        assertEquals(0, resp.failed());
        assertNotNull(resp.errors());
        assertTrue(resp.errors().isEmpty());

        verify(createUserUseCase, times(3)).execute(any(RequestUserData.class));
        verifyNoMoreInteractions(createUserUseCase);
    }

    @Test
    void deveColetarErros_paraExcecoesConhecidas_eContabilizarFalhas() throws Exception, FieldIsEmptyException, RegexpException {
        // Ordem importa para validar ordenação dos erros
        ImportUserRequestData a = dto("111.111.111-11"); // AlreadyExists
        ImportUserRequestData b = dto("222.222.222-22"); // FieldIsEmpty
        ImportUserRequestData c = dto("333.333.333-33"); // Regexp
        ImportUserRequestData d = dto("444.444.444-44"); // sucesso

        when(createUserUseCase.execute(any(RequestUserData.class)))
                .thenAnswer(inv -> {
                    RequestUserData r = inv.getArgument(0);
                    String cpf = r != null ? r.cpf() : null;
                    if (a.cpf().equals(cpf)) throw new AlreadyExistsException("CPF duplicado");
                    if (b.cpf().equals(cpf)) throw new FieldIsEmptyException("nome");
                    if (c.cpf().equals(cpf)) throw new RegexpException("Email inválido");
                    return mock(User.class); // d ou outros: sucesso
                });

        ImportResponseData resp = useCase.execute(List.of(a, b, c, d));

        assertEquals(4, resp.totalRows());
        assertEquals(1, resp.imported());
        assertEquals(3, resp.failed());
        assertEquals(3, resp.errors().size());

        // Ordem e conteúdo dos erros
        assertEquals(a.cpf(), resp.errors().get(0).identifier());
        assertTrue(resp.errors().get(0).message().toLowerCase().contains("duplicado"));

        assertEquals(b.cpf(), resp.errors().get(1).identifier());
        assertTrue(resp.errors().get(1).message().toLowerCase().contains("vazio"));

        assertEquals(c.cpf(), resp.errors().get(2).identifier());
        assertTrue(resp.errors().get(2).message().toLowerCase().contains("inválido"));

        verify(createUserUseCase, times(4)).execute(any(RequestUserData.class));
        verifyNoMoreInteractions(createUserUseCase);
    }

    @Test
    void deveTratarExcecaoGenerica_comPrefixoErroInesperado() throws Exception, FieldIsEmptyException, RegexpException {
        ImportUserRequestData a = dto("555.555.555-55"); // vai lançar RuntimeException
        ImportUserRequestData b = dto("666.666.666-66"); // sucesso

        when(createUserUseCase.execute(any(RequestUserData.class)))
                .thenAnswer(inv -> {
                    RequestUserData r = inv.getArgument(0);
                    String cpf = r != null ? r.cpf() : null;
                    if (a.cpf().equals(cpf)) throw new RuntimeException("boom");
                    return mock(User.class);
                });

        ImportResponseData resp = useCase.execute(List.of(a, b));

        assertEquals(2, resp.totalRows());
        assertEquals(1, resp.imported());
        assertEquals(1, resp.failed());
        assertEquals(1, resp.errors().size());

        ImportErrorDetails err = resp.errors().get(0);
        assertEquals(a.cpf(), err.identifier());
        assertTrue(err.message().startsWith("Erro inesperado: "));
        assertTrue(err.message().contains("boom"));

        verify(createUserUseCase, times(2)).execute(any(RequestUserData.class));
        verifyNoMoreInteractions(createUserUseCase);
    }

    @Test
    void deveTratarElementoNuloNaLista_comFailedEIdentifierNulo() throws Exception, FieldIsEmptyException, RegexpException {
        // Lista com elemento nulo no meio; List.of não aceita null → use Arrays.asList
        ImportUserRequestData a = dto("777.777.777-77"); // sucesso
        ImportUserRequestData c = dto("888.888.888-88"); // sucesso

        when(createUserUseCase.execute(any(RequestUserData.class))).thenReturn(mock(User.class));

        ImportResponseData resp = useCase.execute(Arrays.asList(a, null, c));

        // totalRows conta 3 (inclui o nulo)
        assertEquals(3, resp.totalRows());
        // imported = 2 (apenas os válidos)
        assertEquals(2, resp.imported());
        // failed = 1 (o nulo cai no catch genérico com NPE ao construir RequestUserData)
        assertEquals(1, resp.failed());
        assertEquals(1, resp.errors().size());

        ImportErrorDetails err = resp.errors().get(0);
        assertNull(err.identifier(), "Para dto nulo, o identifier deve ser null");
        assertTrue(err.message().startsWith("Erro inesperado: "));

        verify(createUserUseCase, times(2)).execute(any(RequestUserData.class));
        verifyNoMoreInteractions(createUserUseCase);
    }
}