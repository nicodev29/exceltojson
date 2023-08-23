package com.example.conversor.service;


import com.example.conversor.model.Sucursal;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
public class ExcelService {

    public List<Sucursal> processExcelFile(InputStream file) throws IOException {
        List<Sucursal> sucursales = new ArrayList<>();

        Map<String, UUID> hubMapping = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Omitir las primeras dos filas (encabezados)
            if (rowIterator.hasNext()) rowIterator.next();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Sucursal sucursal = new Sucursal();

                String type = convertType(getValueOrNullOrTrimmed(row.getCell(1)));
                String parentCode = getValueOrNullOrTrimmed(row.getCell(5));

                if (type != null && type.equals("HUB")) {
                    UUID hubUUID = UUID.randomUUID();
                    sucursal.setId(hubUUID);
                    hubMapping.put(getValueOrNullOrTrimmed(row.getCell(0)), hubUUID);
                } else if (type != null && type.equals("BRANCH")) {
                    UUID parentUUID = hubMapping.get(parentCode);
                    if (parentUUID == null) {
                        throw new IllegalArgumentException("Parent HUB not found for BRANCH with code: " + getValueOrNullOrTrimmed(row.getCell(0)));
                    }
                    sucursal.setParentId(parentUUID);
                }

                sucursal.setCode(getValueOrNullOrTrimmed(row.getCell(0)));
                sucursal.setType(type);
                // ... el resto del código como lo tenías ...

                // Add to list
                sucursales.add(sucursal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucursales;
    }

    private String getValueOrNullOrTrimmed(Cell cell) {
        if (cell == null) {
            return null;
        }
        String value = cell.toString().trim();
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            // ... handle other cell types if needed
            default:
                return cell.toString();
        }
    }

    private String convertType(String excelType) {
        switch (excelType) {
            case "CT":
            case "EC":
                return "HUB";
            case "NG":
            case "SA":
            case "SE":
            case "SF":
            case "SN":
                return "BRANCH";
            case "SL":
                return "LOCKER";
            case "UP":
                return "AGENCY";
            default:
                return null;  // o puedes manejar algún valor por defecto o lanzar una excepción
        }
    }


}

