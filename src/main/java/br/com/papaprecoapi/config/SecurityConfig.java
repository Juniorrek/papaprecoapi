package br.com.papaprecoapi.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = { "/h2-console/**", "/produtos/**" };

    // These are property references, so the key location stays configurable.
    // Spring Security's RSA key converters resolve the value as a resource
    // location, which is why a 'file:' or 'classpath:' string binds directly
    // to a key here.
    @Value("${jwt.rsa.pub}")
    private RSAPublicKey publicKey;

    @Value("${jwt.rsa.priv}")
    private RSAPrivateKey privateKey;

    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
            .csrf(csrf -> csrf.disable())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((authorize) -> authorize
                // Probed by the container HEALTHCHECK and by Compose, neither of
                // which can authenticate. Only 'health' is opened: the other
                // actuator endpoints expose configuration and stay protected.
                .requestMatchers("/actuator/health").permitAll()
                // Not an endpoint anyone calls. When a request fails anywhere —
                // a 404 for a path that does not exist, a 500 from a handler —
                // the servlet container re-dispatches it to /error to render the
                // response body. That dispatch goes through this filter chain
                // again, so while /error is authenticated the container's answer
                // to every unhandled failure is 401. The status that reaches the
                // client then describes the error page's access rules rather
                // than the original fault, and every such bug reads as a broken
                // token. Permitting it is safe: server.error.include-message and
                // include-stacktrace default to 'never', so the body is
                // timestamp, status, error and path, and nothing else.
                .requestMatchers("/error").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/notification/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/produtos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/produtos").authenticated()
                //.requestMatchers(WHITE_LIST_URL).permitAll()
				.anyRequest().authenticated()
			);

		return http.build();
	}

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() { 
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwks);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
