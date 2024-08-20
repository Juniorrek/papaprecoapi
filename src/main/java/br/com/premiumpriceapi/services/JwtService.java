package br.com.premiumpriceapi.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import br.com.premiumpriceapi.model.Usuario;

@Service
public class JwtService {
    @Value("${api.security.jwt.secret}")
    private String jwtSecret;

    public String generateJwtToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

            String token = JWT.create()
                    .withIssuer("premiumpriceapi")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            return token;
        } catch(JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

            return JWT.require(algorithm)
                    .withIssuer("premiumpriceapi")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTCreationException exception) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
