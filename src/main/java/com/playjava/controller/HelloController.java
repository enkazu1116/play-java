package com.playjava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {

    @GetMapping("/")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
    
}
