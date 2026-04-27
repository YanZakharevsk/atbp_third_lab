package com.atbp.lab3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/water/page")
    public String waterPage() {
        return "forward:/index.html";
    }

    @GetMapping("/water")
    public String water() {
        return "forward:/index.html";
    }
}