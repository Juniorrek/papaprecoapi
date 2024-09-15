package br.com.papaprecoapi;

import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
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

	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
			new ClassPathResource("papapreco-38a7a-firebase-adminsdk-ys6cr-80c2ce2e1f.json").getInputStream()
		);

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
