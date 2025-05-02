package cit.edu.mmr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")  // maps to root "/"
    public String verifyBackend() {
        return "Hello There welcome to memoire";
    }
}
