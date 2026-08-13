package br.com.papaprecoapi.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.services.FirebaseMessagingService;
// NotificationMessage and RequestBody are imported by send-notification only,
// which is commented out below; restore both if it is ever brought back.

@CrossOrigin
@RestController
@RequestMapping("notification")
public class NotificationController {

    private final FirebaseMessagingService firebaseMessagingService;

    public NotificationController(FirebaseMessagingService firebaseMessagingService) {
        this.firebaseMessagingService = firebaseMessagingService;
    }

    // Deliberately not exposed. This took a caller-supplied recipient token,
    // title and body and pushed them verbatim, so it was an open relay for
    // sending arbitrary notifications to any device whose token was known —
    // and it sat behind a permitAll rule until the alert sweep was scheduled.
    // Nothing in either repository ever called it. If a use for it appears,
    // it needs an authorization check of its own rather than just this being
    // uncommented, because being authenticated is not the same as being
    // entitled to push to another user's device.
    //
    // @PostMapping("/send-notification")
    // public String sendNotification(@RequestBody NotificationMessage notificationMessage) {
    //     return firebaseMessagingService.sendNotificationByToken(notificationMessage);
    // }

    // Kept for debugging: runs the same sweep the 08:00/20:00 schedule runs.
    // Authenticated — see SecurityConfig.
    @PostMapping("/trigger-manual")
    public ResponseEntity<String> verificarEEnviarNotificacoes() {
        // Without this the endpoint answers 200 "sent!" on an instance that has
        // no Firebase credentials and sent nothing at all.
        if (!firebaseMessagingService.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Push notifications are disabled: no Firebase credentials are configured.");
        }

        firebaseMessagingService.verificarAlertasEEnviarNotificacoes();
        return ResponseEntity.ok("Notificações enviadas!");
    }
}
