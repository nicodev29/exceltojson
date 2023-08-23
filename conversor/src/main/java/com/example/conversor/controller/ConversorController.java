package com.example.conversor.controller;

import com.example.conversor.model.Sucursal;
import com.example.conversor.service.ExcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Convertir la lista de sucursales a JSON
        String json = new ObjectMapper().writeValueAsString(sucursales);

        byte[] isr = json.getBytes();

        // Establecer las cabeceras para la descarga del archivo
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sucursales.json");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON).body(isr);
    }
}
