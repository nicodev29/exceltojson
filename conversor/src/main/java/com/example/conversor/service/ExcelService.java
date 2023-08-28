package com.example.conversor.service;

import com.example.conversor.model.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class ExcelService {

    private static final int INDEX_HUB_ID = 0;
    private static final int INDEX_CODE = 1;
    private static final int INDEX_TYPE = 2;
    private static final int INDEX_NUMBER = 3;
    private static final int INDEX_NAME = 4;
    private static final int INDEX_EMAIL_ADDRESS = 5;
    private static final int INDEX_ADDRESS_LINE1 = 8;
    private static final int INDEX_CITY = 4;
    private static final int INDEX_REGION = 11;
    private static final int INDEX_LATITUDE = 12;
    private static final int INDEX_LONGITUDE = 13;
    private static final int INDEX_POSTAL_CODE = 14;
    private static final int INDEX_MANAGER = 15;
    private static final int INDEX_PHONE_NUMBER = 16;
    private static final int INDEX_NUMERATION = 9;
    private static final int INDEX_MONDAY = 17;
    private static final int INDEX_TUESDAY = 18;
    private static final int INDEX_WEDNESDAY = 19;
    private static final int INDEX_THURSDAY = 20;
    private static final int INDEX_FRIDAY = 21;
    private static final int INDEX_SATURDAY = 22;
    private static final int INDEX_SUNDAY = 23;

    public List<Sucursal> processExcelFile(InputStream file) throws IOException {
        List<Sucursal> sucursales = new ArrayList<>();
        Map<String, UUID> hubMapping = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Omitimos las dos primeras filas de encabezado
            if (rowIterator.hasNext()) rowIterator.next();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Sucursal sucursal = createSucursalFromRow(row, hubMapping);

                if (sucursal != null) {
                    sucursales.add(sucursal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucursales;
    }

    private Sucursal createSucursalFromRow(Row row, Map<String, UUID> hubMapping) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(UUID.randomUUID());  // Asigna un UUID a cada sucursal

        // Mapeo de campos
        sucursal.setCode(getValueOrNullOrTrimmed(row.getCell(INDEX_CODE)));
        sucursal.setType(convertType(getValueOrNullOrTrimmed(row.getCell(INDEX_TYPE))));
        sucursal.setNumber(getValueOrNullOrTrimmed(row.getCell(INDEX_NUMBER)));
        sucursal.setName(getValueOrNullOrTrimmed(row.getCell(INDEX_NAME)));
        sucursal.setManager(getValueOrNullOrTrimmed(row.getCell(INDEX_MANAGER)));
        sucursal.setPhoneNumber(getValueOrNullOrTrimmed(row.getCell(INDEX_PHONE_NUMBER)));
        sucursal.setCreatedAt(LocalDateTime.now());
        sucursal.setUpdatedAt(LocalDateTime.now());

        String statusCellValue = getValueOrNullOrTrimmed(row.getCell(6));
        if (statusCellValue != null) {
            sucursal.setStatus(mapStatus(statusCellValue));
        }

        sucursal.setEmailAddress(mapNullToNull(getValueOrNullOrTrimmed(row.getCell(INDEX_EMAIL_ADDRESS))));

        Address address = createAddressFromRow(row);
        sucursal.setAddress(address);

        mapParentId(sucursal, row, hubMapping);

        sucursal.setAttributes(extractAttributes(row));
        return sucursal;
    }

    private String mapStatus(String statusCellValue) {
        switch (statusCellValue) {
            case "S":
                return "ACTIVE";
            case "N":
                return "INACTIVE";
            case "F. BAJA":
                return "CLOSED";
            default:
                throw new IllegalArgumentException("Valor no reconocido para el estado: " + statusCellValue);
        }
    }

    private void mapParentId(Sucursal sucursal, Row row, Map<String, UUID> hubMapping) {
        String parentCode = getValueOrNullOrTrimmed(row.getCell(0));
        String currentCode = getValueOrNullOrTrimmed(row.getCell(1));
        String type = getValueOrNullOrTrimmed(row.getCell(2));

        if ("HUB".equals(type)) {
            UUID hubUUID = UUID.randomUUID();
            hubMapping.put(currentCode, hubUUID);
        } else if ("BRANCH".equals(type)) {
            UUID parentUUID = hubMapping.get(parentCode);
            if (parentUUID == null) {
                throw new IllegalArgumentException("Parent HUB not found for BRANCH with code: " + currentCode);
            }
            sucursal.setHub_id(parentUUID);
        }
    }

    private Address createAddressFromRow(Row row) {
        Address address = new Address();
        String addressLine1 = getValueOrNullOrTrimmed(row.getCell(INDEX_ADDRESS_LINE1));
        String numeration = getValueOrNullOrTrimmed(row.getCell(INDEX_NUMERATION));

        if (numeration != null && !numeration.trim().isEmpty()) {
            addressLine1 = mapNumeration(addressLine1, numeration);
        }

        address.setAddressLine1(addressLine1);
        address.setCity(getValueOrNullOrTrimmed(row.getCell(INDEX_CITY)));
        address.setRegion(mapNullToRegion(getValueOrNullOrTrimmed(row.getCell(INDEX_REGION))));
        address.setPostalCode(getValueOrNullOrTrimmed(row.getCell(INDEX_POSTAL_CODE)));
        address.setCountry("AR");

        Location location = createLocationFromRow(row);
        address.setLocation(location);

        return address;
    }

    private Location createLocationFromRow(Row row) {
        Location location = new Location();
        location.setType("Point");

        Cell latitudeCell = row.getCell(INDEX_LATITUDE);
        Cell longitudeCell = row.getCell(INDEX_LONGITUDE);

        String latitudeValue = getValueOrNullOrTrimmed(latitudeCell);
        String longitudeValue = getValueOrNullOrTrimmed(longitudeCell);

        if (latitudeValue != null && longitudeValue != null) {
            try {
                Double latitude = Double.parseDouble(latitudeValue);
                Double longitude = Double.parseDouble(longitudeValue);

                List<Double> coordinatesList = new ArrayList<>();
                coordinatesList.add(latitude);
                coordinatesList.add(longitude);

                location.setCoordinates(coordinatesList);
            } catch (NumberFormatException e) {
                location.setCoordinates(null);
            }
        }
        return location;
    }

    private Attributes extractAttributes(Row row) {
        Attributes attributes = new Attributes();
        Map<String, OpeningHours> openingHoursMap = new HashMap<>();
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

        int[] indices = {
                INDEX_MONDAY, INDEX_TUESDAY, INDEX_WEDNESDAY, INDEX_THURSDAY, INDEX_FRIDAY, INDEX_SATURDAY, INDEX_SUNDAY
        };

        for (int i = 0; i < indices.length; i++) {
            OpeningHours openingHours = extractOpeningHoursFromCell(row.getCell(indices[i]));
            if (openingHours != null) {
                openingHoursMap.put(days[i], openingHours);
            }
        }

        attributes.setOpeningHours(openingHoursMap);
        return attributes;
    }

    private OpeningHours extractOpeningHoursFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        String value = cell.toString().trim();
        if (value.isEmpty()) {
            return null;
        }

        String[] parts = value.split(" A ");
        if (parts.length != 2) {
            // Puedes manejar el error aquí si es necesario
            return null;
        }

        String opens = parts[0] + ":00";
        String closes = parts[1] + ":00";

        OpeningHours openingHours = new OpeningHours();
        openingHours.setOpens(opens);
        openingHours.setCloses(closes);

        return openingHours;
    }


    private String mapNumeration(String addressLine1, String numeration) {
        try {
            double numerationValue = Double.parseDouble(numeration);
            int numerationIntValue = (int) numerationValue;
            return addressLine1 + " " + numerationIntValue;
        } catch (NumberFormatException e) {
            return addressLine1 + " " + numeration;
        }
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

    private String convertType(String excelType) throws IllegalArgumentException {
        if (excelType == null) {
            throw new IllegalArgumentException("El tipo excelType no puede ser nulo.");
        }
        switch (excelType) {
            case "CT":
            case "EC":
                return "HUB";
            case "NG":
            case "SA":
            case "SE":
            case "SF":
            case "SN":
            case "SB":
                return "BRANCH";
            case "SL":
                return "LOCKER";
            case "UP":
                return "AGENCY";
            default:
                throw new IllegalArgumentException("Tipo no reconocido: " + excelType);
        }
    }

    private String mapNullToNull(String value) {
        if ("NULL".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private String mapNullToRegion(String value) {
        if ("NULL".equalsIgnoreCase(value)) {
            return null;
        }
        return "AR-" + value;
    }
}

