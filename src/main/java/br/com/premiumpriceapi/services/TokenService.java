package br.com.premiumpriceapi.services;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.premiumpriceapi.repository.UsuarioRepository;

@Service
public class TokenService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    public String gerarTokenVerificarEmailUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (usuarioRepository.existsByTokenVerificarEmail(token));
        return token;
    }

    public String gerarRandomAlphanumeric6Token() {
        return gerarRandomAlphanumericToken(6);
    }

    public String gerarRandomAlphanumericToken(int tamanho) {
        return RandomStringUtils.randomAlphanumeric(tamanho);
    }
}
