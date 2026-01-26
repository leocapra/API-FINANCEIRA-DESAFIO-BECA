package br.com.beca.userservice.infrastructure.web.controller;

import br.com.beca.userservice.application.usecase.*;
import br.com.beca.userservice.domain.dto.*;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import br.com.beca.userservice.infrastructure.gateway.ExcelUserImportMapper;
import br.com.beca.userservice.infrastructure.gateway.PageMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearer-key")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DetailsUserUseCase detailsUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ImportUserFromExcelUseCase importUserFromExcelUseCase;
    private final ExcelUserImportMapper excelUserImportMapper;

    public UserController(
            CreateUserUseCase createUserUseCase,
            ListUsersUseCase listUsersUseCase,
            DetailsUserUseCase detailsUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            ImportUserFromExcelUseCase importUserFromExcelUseCase, ExcelUserImportMapper excelUserImportMapper
    ) {
        this.createUserUseCase = createUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.detailsUserUseCase = detailsUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.importUserFromExcelUseCase = importUserFromExcelUseCase;
        this.excelUserImportMapper = excelUserImportMapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity create(@RequestBody @Valid RequestUserData dto, UriComponentsBuilder uriBuilder) throws FieldIsEmptyException, RegexpException {
        User created = createUserUseCase.execute(new User(
                        dto.cpf(),
                        dto.nome(),
                        dto.email(),
                        dto.senha(),
                        dto.telefone()
                )
        );
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(created.getId()).toUri();
        var details = detailsUserUseCase.execute(created.getId());
        Optional<ResponseUserData> detailsConverted = details.map(d -> new ResponseUserData(
                d.getId(),
                d.getCpf(),
                d.getNome(),
                d.getEmail(),
                d.getTelefone()
        ));
        return ResponseEntity.created(uri).body(detailsConverted);
    }

    @GetMapping
    @Transactional
    public ResponseEntity<PaginatedResponse<ResponseUserData>> listUsers(Pageable pageable) {
        var pageableDomain = new PageMapper().pageableToPageDataDomain(pageable);
        return ResponseEntity.ok(listUsersUseCase.execute(pageableDomain).map(u -> new ResponseUserData(
                u.getId(),
                u.getCpf(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone()
        )));
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity detailUser(@PathVariable Long id) {
        var details = detailsUserUseCase.execute(id);
        return ResponseEntity.ok(details.map(u -> new ResponseUserData(
                u.getId(),
                u.getCpf(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone()
        )));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity updateUser(@PathVariable Long id, @RequestBody UpdateUserData dto) throws RegexpException {
        updateUserUseCase.execute(id, dto);
        var details = detailsUserUseCase.execute(id);
        return ResponseEntity.ok(details.map(u -> new ResponseUserData(
                u.getId(),
                u.getCpf(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone()
        )));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteUser(@PathVariable Long id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ImportResponseData> importUsers(@RequestPart("file") MultipartFile file) {

        List<ImportUserRequestData> parsed =
                excelUserImportMapper.map(file);

        ImportResponseData result =
                importUserFromExcelUseCase.execute(parsed);

        return ResponseEntity.ok(result);
    }

}