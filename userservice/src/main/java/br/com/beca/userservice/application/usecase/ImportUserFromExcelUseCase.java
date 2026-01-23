package br.com.beca.userservice.application.usecase;

import br.com.beca.userservice.domain.dto.ImportResponseData;
import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import br.com.beca.userservice.domain.exception.FieldIsEmptyException;
import br.com.beca.userservice.domain.exception.RegexpException;
import br.com.beca.userservice.domain.model.User;

import java.util.List;

public record ImportUserFromExcelUseCase(CreateUserUseCase createUserUseCase) {

    public ImportResponseData execute(List<ImportUserRequestData> dtos) {

        int total = 0, success = 0, failed = 0;

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

            } catch (Exception e) {
                failed++;
            } catch (FieldIsEmptyException e) {
                throw new RuntimeException(e);
            } catch (RegexpException e) {
                throw new RuntimeException(e);
            }
        }

        return new ImportResponseData(total, success, failed);
    }

}
