package br.com.papaprecoapi.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import br.com.papaprecoapi.dto.AlertaProdutoDTO;
import br.com.papaprecoapi.model.NotificationMessage;
import br.com.papaprecoapi.repository.AlertaUsuarioRepository;

@Service
public class FirebaseMessagingService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);

    // required = false, because FirebaseConfig supplies no bean when the
    // service account key is absent. This field is then null and the service
    // reports itself disabled rather than the application refusing to start.
    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private AlertaUsuarioRepository alertaUsuarioRepository;

    /** Whether a Firebase credential was found at startup. */
    public boolean isEnabled() {
        return firebaseMessaging != null;
    }

    public String sendNotificationByToken(NotificationMessage notificationMessage) {
        if (!isEnabled()) {
            log.warn("Ignoring notification '{}': Firebase is not configured.", notificationMessage.getTitle());
            return "Disabled";
        }

        Notification notification = Notification
            .builder()
            .setTitle(notificationMessage.getTitle())
            .setBody(notificationMessage.getBody())
            .build();

        Message message = Message
            .builder()
            .setToken(notificationMessage.getRecipientToken())
            .setNotification(notification)
            .putAllData(notificationMessage.getData())
            .build();

        try {
            firebaseMessaging.send(message);
            return "Success";
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public void verificarAlertasEEnviarNotificacoes() {
        if (!isEnabled()) {
            // Returning before the query, rather than after: with no way to
            // deliver anything, running it would be work for nothing.
            log.warn("Skipping the price alert sweep: Firebase is not configured.");
            return;
        }

        List<AlertaProdutoDTO> alertas = alertaUsuarioRepository.findAlertasComProdutosValidos();

        for (AlertaProdutoDTO alerta : alertas) {
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setRecipientToken(alerta.getFcmToken()); 
            notificationMessage.setTitle("Alerta de preço");
            notificationMessage.setBody(String.format("O produto %s está por R$%.2f!", alerta.getNomeProduto(), alerta.getPrecoProduto()));

            // Dados adicionais (se necessário)
            Map<String, String> data = new HashMap<>();
            data.put("page", "produtos");
            data.put("palavra", String.valueOf(alerta.getNomeProduto()));
            /*data.put("produtoId", String.valueOf(alerta.getProdutoId()));
            data.put("precoProduto", String.valueOf(alerta.getPrecoProduto()));*/
            notificationMessage.setData(data);

            System.out.println(alerta);

            // Enviar a notificação
            sendNotificationByToken(notificationMessage);
        }
    }

    /*private final String FIREBASE_SERVER_KEY = "2pWqyk4XcJoecT8s4fsHjES6VZXAa5MXszJAzMCVGF0";
    private final String FIREBASE_API_URL = "https://fcm.googleapis.com/fcm/send";

    public void sendNotification(String targetToken, String title, String body) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "key=" + FIREBASE_SERVER_KEY);
            headers.set("Content-Type", "application/json");

            // Construindo o corpo da notificação
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("to", targetToken);

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);

            bodyMap.put("notification", notification);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(bodyMap);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // Enviando a requisição para o FCM
            ResponseEntity<String> response = restTemplate.exchange(FIREBASE_API_URL, HttpMethod.POST, entity, String.class);

            // Processando a resposta (opcional)
            System.out.println("Resposta do FCM: " + response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
