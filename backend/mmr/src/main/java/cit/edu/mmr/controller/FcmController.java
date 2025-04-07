package cit.edu.mmr.controller;

import cit.edu.mmr.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@CrossOrigin(origins = "http://localhost:5173")
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


//    @PostMapping("/test-notification")
//    public ResponseEntity<?> sendTestNotification(Authentication auth) {
//        notificationService.createNotification(
//                auth,
//                "Test Notification",
//                "This is a test notification from the server",
//                2L,  // relatedItemId can be null for tests
//                "test" // itemType
//        );
//        return ResponseEntity.ok().build();
//    }
}