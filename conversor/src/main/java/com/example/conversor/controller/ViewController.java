package com.example.conversor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";  // nombre del archivo HTML sin la extensión
    }
}
