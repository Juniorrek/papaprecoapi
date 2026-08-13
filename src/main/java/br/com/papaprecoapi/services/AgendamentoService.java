package br.com.papaprecoapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    // Sweeps every user's price alerts twice a day, at 08:00 and 20:00, and
    // pushes a notification for each one now matched by a reported price.
    // Scheduling is active application-wide via @EnableScheduling on
    // PapaprecoapiApplication; without that annotation this method is inert.
    //
    // The zone is explicit and load-bearing. Without it the cron resolves
    // against the JVM's default timezone, and the container sets no TZ, so it
    // runs UTC: these times landed at 05:00 and 17:00 in Brazil, meaning a
    // push before dawn for every user the app has. The expression is written
    // in the users' local time, so it has to be read in it too.
    //
    // Safe on an instance with no Firebase credentials: the service reports
    // isEnabled() false and returns before running its query, so this logs a
    // warning rather than failing on every tick.
    @Scheduled(cron = "0 0 8,20 * * ?", zone = "America/Sao_Paulo")
    public void executarVerificacaoDeAlertas() {
        firebaseMessagingService.verificarAlertasEEnviarNotificacoes();
    }
}