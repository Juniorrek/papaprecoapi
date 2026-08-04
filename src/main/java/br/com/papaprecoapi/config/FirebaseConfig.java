package br.com.papaprecoapi.config;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Firebase Cloud Messaging, used to push price alerts to the app.
 *
 * <p>The service account key this needs is a server-side admin credential for
 * the whole Firebase project. It cannot be committed, and unlike the JWT
 * keypair it cannot be generated with a shell command either — it has to be
 * downloaded from the Firebase console by someone with access to the project.
 *
 * <p>Push notifications are one optional feature, so their credential must not
 * be a prerequisite for starting the API. When the key is missing this bean is
 * null, the messaging service reports itself disabled, and everything else —
 * search, ranking, authentication — runs normally. That is what lets someone
 * clone the repository and get a working stack out of {@code docker compose up}
 * without being handed a production credential first.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private static final String APP_NAME = "papapreco";

    @Value("${firebase.credentials.location}")
    private Resource credentials;

    @Bean
    FirebaseMessaging firebaseMessaging() {
        if (!credentials.exists()) {
            // Expected on a fresh checkout, so this is a warning rather than a
            // failure. It is logged loudly because the alternative — silently
            // dropping every notification — is the harder thing to diagnose.
            log.warn("Firebase credentials not found at {}. Push notifications are disabled; "
                    + "everything else starts normally. To enable them, put a service account key "
                    + "there or set FIREBASE_CREDENTIALS_LOCATION.", describeLocation());
            return null;
        }

        try (InputStream credentialsStream = credentials.getInputStream()) {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(credentialsStream);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(googleCredentials).build();

            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options, APP_NAME)
                    : FirebaseApp.getInstance(APP_NAME);

            log.info("Firebase messaging initialised from {}", describeLocation());
            return FirebaseMessaging.getInstance(app);
        } catch (IOException | RuntimeException e) {
            // A file that exists but cannot be read is a misconfiguration rather
            // than an absent optional feature, so it is logged at ERROR — but it
            // still does not stop the application, on the same reasoning as
            // above: notifications do not get to take the API down with them.
            log.error("Firebase credentials at {} could not be read. Push notifications are disabled.",
                    describeLocation(), e);
            return null;
        }
    }

    private String describeLocation() {
        try {
            return credentials.getURL().toString();
        } catch (IOException e) {
            return credentials.getDescription();
        }
    }
}
