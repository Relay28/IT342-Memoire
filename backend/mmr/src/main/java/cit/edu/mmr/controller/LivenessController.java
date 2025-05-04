package cit.edu.mmr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LivenessController {

    @GetMapping("/")
    public String healthCheck() {
        return "Service is up and running!";
    }

    @GetMapping("/_ah/health")
    public String appEngineHealthCheck() {
        return "healthy";
    }
}