package cit.edu.mmr.controller;

import cit.edu.mmr.dto.CountdownDTO;
import cit.edu.mmr.service.CountdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capsules")
public class CountdownController {

    private final CountdownService countdownService;

    @Autowired
    public CountdownController(CountdownService countdownService) {
        this.countdownService = countdownService;
    }

    @GetMapping("/{id}/countdown")
    public ResponseEntity<CountdownDTO> getCountdown(@PathVariable Long id) {
        CountdownDTO countdown = countdownService.getTimeUntilOpening(id);
        return ResponseEntity.ok(countdown);
    }
}