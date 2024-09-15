package br.com.papaprecoapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    // Agendar para 8h da manhã e 8h da noite
    /*@Scheduled(cron = "0 0 8,20 * * ?")
    public void executarVerificacaoDeAlertas() {
        firebaseMessagingService.verificarAlertasEEnviarNotificacoes();
    }*/
}