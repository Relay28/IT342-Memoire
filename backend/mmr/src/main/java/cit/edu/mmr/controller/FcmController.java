package cit.edu.mmr.controller;

import cit.edu.mmr.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@CrossOrigin(origins = "https://it-342-memoire.vercel.app/")
@RequiredArgsConstructor

public class FcmController {
    @Autowired
    private final NotificationService notificationService;


    @PostMapping("/update-token")
    public ResponseEntity<Void> updateFcmToken(
            @RequestParam Long userId,
            @RequestParam String fcmToken,
            Authentication auth
    ) {
        notificationService.updateFcmToken(auth, fcmToken);
        return ResponseEntity.ok().build();
    }
}