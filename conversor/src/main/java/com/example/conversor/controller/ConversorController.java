package com.example.conversor.controller;

import com.example.conversor.model.Sucursal;
import com.example.conversor.service.ExcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;


import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class ConversorController {

    private final ExcelService excelService;

    @Autowired
    public ConversorController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        List<Sucursal> sucursales = excelService.processExcelFile(file.getInputStream());

        // Instanciar el ObjectMapper, registrar el módulo y desactivar la serialización de fechas como timestamps
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Convertir la lista de sucursales a JSON con "pretty print"
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sucursales);

        byte[] isr = json.getBytes();

        // Establecer las cabeceras para la descarga del archivo
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sucursales.json");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON).body(isr);
    }
}
