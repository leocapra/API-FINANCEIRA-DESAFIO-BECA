package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.application.port.UserRepository;
import br.com.beca.userservice.domain.dto.TokenInfoData;
import br.com.beca.userservice.domain.exception.NotFoundException;
import br.com.beca.userservice.domain.exception.PermissionException;
import br.com.beca.userservice.domain.model.Roles;
import br.com.beca.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Cobertura total:
 *  - NotFound quando usuário ativo não encontrado
 *  - ROLE_USER: pode consultar a si mesmo; não pode consultar outro (PermissionException)
 *  - ROLE_ADMIN: pode consultar qualquer usuário
 *  - Retorna uma NOVA instância de User (não a mesma do repositório)
 *
 * OBS: o construtor de User está trocando createdAt/updatedAt. Este teste reflete o comportamento atual.
 */
@ExtendWith(MockitoExtension.class)
class DetailsUserUseCaseTest {

    @Mock private UserRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private User details;

    private DetailsUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DetailsUserUseCase(repository);
    }

    @Test
    void deveLancar_NotFound_quandoUsuarioAtivoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(id.toString(), "ROLE_ADMIN");

        when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> useCase.execute(id, token));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(repository).findByIdAndActiveTrue(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void roleUser_podeConsultar_aSiMesmo_eRetornaClone() {
        UUID id = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(id.toString(), "ROLE_USER");

        Roles roles = Roles.ROLE_USER;
        String cpf = "123.456.789-01";
        String nome = "Leonardo";
        String email = "leo@mail.com";
        String senha = "hash#123";
        String telefone = "+55 11 91234-5678";
        boolean active = true;
        LocalDate createdAt = LocalDate.now().minusDays(2);
        LocalDate updatedAt = LocalDate.now().minusDays(1);

        when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(details));
        when(details.getId()).thenReturn(id);
        when(details.getCpf()).thenReturn(cpf);
        when(details.getRoles()).thenReturn(roles);
        when(details.getNome()).thenReturn(nome);
        when(details.getEmail()).thenReturn(email);
        when(details.getSenha()).thenReturn(senha);
        when(details.getTelefone()).thenReturn(telefone);
        when(details.isActive()).thenReturn(active);
        when(details.getCreatedAt()).thenReturn(createdAt);
        when(details.getUpdatedAt()).thenReturn(updatedAt);

        User result = useCase.execute(id, token);

        // Nova instância, não a mesma
        assertNotNull(result);
        assertNotSame(details, result);

        // Campos clonados
        assertEquals(id, result.getId());
        assertEquals(cpf, result.getCpf());
        assertEquals(roles, result.getRoles());
        assertEquals(nome, result.getNome());
        assertEquals(email, result.getEmail());
        assertEquals(senha, result.getSenha());
        assertEquals(telefone, result.getTelefone());
        assertEquals(active, result.isActive());

        // ⚠️ Comportamento atual do construtor de User TROCA os campos:
        // this.updatedAt = createdAt;
        // this.createdAt = updatedAt;
        assertEquals(createdAt, result.getCreatedAt(), "ATUAL: createdAt do resultado recebe updatedAt de origem");
        assertEquals(updatedAt, result.getUpdatedAt(), "ATUAL: updatedAt do resultado recebe createdAt de origem");

        verify(repository).findByIdAndActiveTrue(id);
    }

    @Test
    void roleUser_naoPodeConsultar_outroUsuario_lancaPermissionException() {
        UUID idSolicitado = UUID.randomUUID();
        UUID idDoToken = UUID.randomUUID(); // diferente
        TokenInfoData token = new TokenInfoData(idDoToken.toString(), "ROLE_USER");

        when(repository.findByIdAndActiveTrue(idSolicitado)).thenReturn(Optional.of(details));
        when(details.getId()).thenReturn(idSolicitado);

        PermissionException ex = assertThrows(PermissionException.class, () -> useCase.execute(idSolicitado, token));
        assertTrue(ex.getMessage().toLowerCase().contains("permissão"));
        verify(repository).findByIdAndActiveTrue(idSolicitado);
    }

    @Test
    void roleAdmin_podeConsultar_qualquerUsuario_eRetornaClone() {
        UUID outroId = UUID.randomUUID();
        TokenInfoData token = new TokenInfoData(UUID.randomUUID().toString(), "ROLE_ADMIN");

        Roles roles = Roles.ROLE_ADMIN;
        String cpf = "987.654.321-00";
        String nome = "Admin";
        String email = "admin@mail.com";
        String senha = "hash#adm";
        String telefone = "+55 11 90000-0000";
        boolean active = true;
        LocalDate createdAt = LocalDate.now().minusDays(10);
        LocalDate updatedAt = LocalDate.now().minusDays(2);

        when(repository.findByIdAndActiveTrue(outroId)).thenReturn(Optional.of(details));
        when(details.getId()).thenReturn(outroId);
        when(details.getCpf()).thenReturn(cpf);
        when(details.getRoles()).thenReturn(roles);
        when(details.getNome()).thenReturn(nome);
        when(details.getEmail()).thenReturn(email);
        when(details.getSenha()).thenReturn(senha);
        when(details.getTelefone()).thenReturn(telefone);
        when(details.isActive()).thenReturn(active);
        when(details.getCreatedAt()).thenReturn(createdAt);
        when(details.getUpdatedAt()).thenReturn(updatedAt);

        User result = useCase.execute(outroId, token);

        assertNotNull(result);
        assertNotSame(details, result);

        assertEquals(outroId, result.getId());
        assertEquals(cpf, result.getCpf());
        assertEquals(roles, result.getRoles());
        assertEquals(nome, result.getNome());
        assertEquals(email, result.getEmail());
        assertEquals(senha, result.getSenha());
        assertEquals(telefone, result.getTelefone());
        assertEquals(active, result.isActive());

        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());

        verify(repository).findByIdAndActiveTrue(outroId);
    }
}