package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.domain.dto.ImportErrorDetails;
import br.com.beca.userservice.domain.dto.ImportResponseData;
import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import br.com.beca.userservice.domain.dto.RequestUserData;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;

import java.util.ArrayList;
import java.util.List;

public record ImportUserFromExcelUseCase(CreateUserUseCase createUserUseCase) {

    public ImportResponseData execute(List<ImportUserRequestData> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ImportResponseData(0, 0, 0, List.of());
        }

        int totalRows = 0;
        int imported = 0;
        int failed = 0;
        List<ImportErrorDetails> errors = new ArrayList<>();

        for (ImportUserRequestData dto : dtos) {
            totalRows++;

            String identifier = null;
            if (dto != null && dto.cpf() != null) {
                identifier = dto.cpf().trim();
            }

            try {
                RequestUserData newDto = new RequestUserData(
                        dto.cpf(),
                        dto.nome(),
                        dto.email(),
                        dto.senha(),
                        dto.telefone()
                );

                createUserUseCase.execute(newDto);
                imported++;

            } catch (AlreadyExistsException | FieldIsEmptyException | RegexpException e) {
                failed++;
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                errors.add(new ImportErrorDetails(identifier, msg));

            } catch (Exception e) {
                failed++;
                String base = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                errors.add(new ImportErrorDetails(identifier, "Erro inesperado: " + base));
            }
        }

        return new ImportResponseData(totalRows, imported, failed, errors);
    }
}