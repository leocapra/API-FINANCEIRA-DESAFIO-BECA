package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.domain.dto.ImportErrorDetails;
import br.com.beca.userservice.domain.dto.ImportResponseData;
import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import br.com.beca.userservice.domain.exception.AlreadyExistsException;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ImportUserFromExcelUseCase(CreateUserUseCase createUserUseCase) {

        public ImportResponseData execute(List<ImportUserRequestData> dtos) {
            int total = 0, success = 0, failed = 0;
            List<ImportErrorDetails> errorList = new ArrayList<>();

            for (ImportUserRequestData dto : dtos) {
                total++;
                try {
                    User user = new User(
                            dto.cpf(),
                            dto.nome(),
                            dto.email(),
                            dto.senha(),
                            dto.telefone()
                    );

                    createUserUseCase.execute(user);
                    success++;

                } catch (AlreadyExistsException | FieldIsEmptyException | RegexpException e) {
                    failed++;
                    errorList.add(new ImportErrorDetails(dto.cpf(), e.getMessage()));
                } catch (Exception e) {
                    failed++;
                    errorList.add(new ImportErrorDetails(dto.cpf(), "Erro inesperado: " + e.getMessage()));
                }
            }

            return new ImportResponseData(total, success, failed, errorList);
        }
    }
