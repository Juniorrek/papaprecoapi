package br.com.papaprecoapi;

import java.io.IOException;
import java.io.InputStream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@SpringBootApplication
@EnableScheduling
public class PapaprecoapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PapaprecoapiApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	// Service account key for the Firebase Admin SDK. Kept out of the classpath
	// so it is never packaged into the artifact.
	@Value("${firebase.credentials.location}")
	private Resource firebaseCredentials;

	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
		GoogleCredentials googleCredentials;
		try (InputStream credentialsStream = firebaseCredentials.getInputStream()) {
			googleCredentials = GoogleCredentials.fromStream(credentialsStream);
		}

		FirebaseOptions firebaseOptions = FirebaseOptions.builder()
			.setCredentials(googleCredentials).build();

		FirebaseApp app;
		if (FirebaseApp.getApps().isEmpty()) {
			app = FirebaseApp.initializeApp(firebaseOptions, "papapreco");
		} else {
			app = FirebaseApp.getInstance("papapreco");
		}
	
		return FirebaseMessaging.getInstance(app);
	}

}
