package br.com.papaprecoapi.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.model.NotificationMessage;
import br.com.papaprecoapi.services.FirebaseMessagingService;

@CrossOrigin
@RestController
@RequestMapping("notification")
public class NotificationController {

    private final FirebaseMessagingService firebaseMessagingService;

    public NotificationController(FirebaseMessagingService firebaseMessagingService) {
        this.firebaseMessagingService = firebaseMessagingService;
    }

    @PostMapping("/send-notification")
    public String sendNotification(@RequestBody NotificationMessage notificationMessage) {
        return firebaseMessagingService.sendNotificationByToken(notificationMessage);
    }

    @PostMapping("/trigger-manual")
    public ResponseEntity<String> verificarEEnviarNotificacoes() {
        firebaseMessagingService.verificarAlertasEEnviarNotificacoes();
        return ResponseEntity.ok("Notificações enviadas!");
    }
}
