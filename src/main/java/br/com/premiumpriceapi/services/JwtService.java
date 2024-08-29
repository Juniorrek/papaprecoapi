package br.com.premiumpriceapi.services;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        Long expiresIn = 3600L;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuer("premiumpriceapi")
                        .issuedAt(now)
                        .subject(email)
                        .expiresAt(now.plusSeconds(expiresIn))
                        .build();

        String jwtToken = jwtEncoder
                        .encode(JwtEncoderParameters.from(claims))
                        .getTokenValue();

        return jwtToken;
    }
}
