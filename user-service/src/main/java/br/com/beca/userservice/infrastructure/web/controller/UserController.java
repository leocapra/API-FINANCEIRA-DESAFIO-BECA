package br.com.beca.userservice.infrastructure.web.controller;

import br.com.beca.userservice.application.usecase.*;
import br.com.beca.userservice.domain.dto.*;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import br.com.beca.userservice.infrastructure.gateway.ExcelUserImportMapper;
import br.com.beca.userservice.infrastructure.gateway.PageMapper;
import br.com.beca.userservice.infrastructure.security.SecurityFilter;
import br.com.beca.userservice.infrastructure.web.service.ExtractInfoFromToken;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

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
    private final User user;
    private final SecurityFilter securityFilter;
    private final ExtractInfoFromToken extractInfoFromToken;

    public UserController(
            CreateUserUseCase createUserUseCase,
            ListUsersUseCase listUsersUseCase,
            DetailsUserUseCase detailsUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            ImportUserFromExcelUseCase importUserFromExcelUseCase,
            ExcelUserImportMapper excelUserImportMapper,
            User user,
            SecurityFilter securityFilter, ExtractInfoFromToken extractInfoFromToken
    ) {
        this.createUserUseCase = createUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.detailsUserUseCase = detailsUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.importUserFromExcelUseCase = importUserFromExcelUseCase;
        this.excelUserImportMapper = excelUserImportMapper;
        this.user = user;
        this.securityFilter = securityFilter;
        this.extractInfoFromToken = extractInfoFromToken;
    }

    @PostMapping("/criar")
    @Transactional
    public ResponseEntity create(@RequestBody @Valid RequestUserData dto, UriComponentsBuilder uriBuilder) throws FieldIsEmptyException, RegexpException {
        User created = createUserUseCase.execute(dto);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(new ResponseUserData(created.getId(), created.getCpf(), created.getNome(), created.getEmail(), created.getTelefone()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity detailUser(@PathVariable UUID id, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        var details = detailsUserUseCase.execute(id, tokenData);
        return ResponseEntity.ok(new ResponseUserData(
                details.getId(),
                details.getCpf(),
                details.getNome(),
                details.getEmail(),
                details.getTelefone()
        ));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity updateUser(@PathVariable UUID id, @RequestBody UpdateUserData dto, HttpServletRequest request) throws RegexpException {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        User user = updateUserUseCase.execute(id, dto, tokenData);
        return ResponseEntity.ok(new ResponseUserData(
                user.getId(),
                user.getCpf(),
                user.getNome(),
                user.getEmail(),
                user.getTelefone()
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        TokenInfoData tokenData = extractInfoFromToken.tokenInfo(request);
        deleteUserUseCase.execute(id, tokenData);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportResponseData> importUsers(@RequestPart("file") MultipartFile file) {

        List<ImportUserRequestData> parsed =
                excelUserImportMapper.map(file);

        ImportResponseData result =
                importUserFromExcelUseCase.execute(parsed);

        return ResponseEntity.ok(result);
    }

}