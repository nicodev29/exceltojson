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
                    switch (statusCellValue) {
                        case "S":
                            sucursal.setStatus("ACTIVE");
                            break;
                        case "N":
                            sucursal.setStatus("INACTIVE");
                            break;
                        case "F. BAJA":
                            sucursal.setStatus("CLOSED");
                            break;
                        default:
                            throw new IllegalArgumentException("Valor no reconocido para el estado: " + statusCellValue);
                    }
                }

                String emailAddress = getValueOrNullOrTrimmed(row.getCell(INDEX_EMAIL_ADDRESS));
                if ("NULL".equalsIgnoreCase(emailAddress)) {
                    emailAddress = null;
                }

                Address address = new Address();
                String addressLine1 = getValueOrNullOrTrimmed(row.getCell(INDEX_ADDRESS_LINE1));
                String numeration = getValueOrNullOrTrimmed(row.getCell(INDEX_NUMERATION));

                if (numeration != null && !numeration.trim().isEmpty()) {
                    try {
                        double numerationValue = Double.parseDouble(numeration);
                        int numerationIntValue = (int) numerationValue; // Convierte a int para quitar los decimales
                        addressLine1 = addressLine1 + " " + numerationIntValue;
                    } catch (NumberFormatException e) {
                        // Si no se puede convertir a número, mantener la numeración como está
                        addressLine1 = addressLine1 + " " + numeration;
                    }
                }

                address.setAddressLine1(addressLine1);
                address.setCity(getValueOrNullOrTrimmed(row.getCell(INDEX_CITY)));
                String regionValue = getValueOrNullOrTrimmed(row.getCell(INDEX_REGION));
                if (regionValue != null) {
                    address.setRegion("AR-" + regionValue);
                } else {
                    address.setRegion(null);
                }
                address.setPostalCode(getValueOrNullOrTrimmed(row.getCell(INDEX_POSTAL_CODE)));
                address.setCountry("AR");

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
                        // No se pudo convertir a Double, se deja como null
                        location.setCoordinates(null);
                    }
                }

                address.setLocation(location);
                sucursal.setAddress(address);

                // Mapeo del parentCode y parentId
                String parentCode = getValueOrNullOrTrimmed(row.getCell(0));
                String currentCode = getValueOrNullOrTrimmed(row.getCell(1));
                String type = getValueOrNullOrTrimmed(row.getCell(2));

                if (type != null && type.equals("HUB")) {
                    UUID hubUUID = UUID.randomUUID();
                    hubMapping.put(currentCode, hubUUID);  // Usa el código de la sucursal actual como clave en el mapeo
                } else if (type != null && type.equals("BRANCH")) {
                    UUID parentUUID = hubMapping.get(parentCode);
                    if (parentUUID == null) {
                        throw new IllegalArgumentException("Parent HUB not found for BRANCH with code: " + currentCode);
                    }
                    sucursal.setHub_id(parentUUID);
                }


                Attributes attributes = sucursal.getAttributes();

                if (attributes == null) {
                    attributes = new Attributes();
                    sucursal.setAttributes(attributes); // Asegúrate de asignar el objeto attributes a la sucursal
                }

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
                sucursal.setAttributes(attributes); // Asegúrate de asignar el objeto attributes de nuevo a la sucursal
                sucursales.add(sucursal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucursales;
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


}

