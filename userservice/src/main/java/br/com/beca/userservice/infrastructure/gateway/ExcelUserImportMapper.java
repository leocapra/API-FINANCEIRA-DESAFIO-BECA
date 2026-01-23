package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.domain.dto.ImportUserRequestData;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUserImportMapper {

    public List<ImportUserRequestData> map(MultipartFile file) {

        List<ImportUserRequestData> users = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                users.add(new ImportUserRequestData(
                        getString(row, 0),
                        getString(row, 1),
                        getString(row, 2),
                        getString(row, 3),
                        getString(row, 4)
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro lendo Excel", e);
        }

        return users;
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? null : new DataFormatter().formatCellValue(cell);
    }
}
