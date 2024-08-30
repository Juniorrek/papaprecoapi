package br.com.papaprecoapi.services;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.papaprecoapi.model.CodigoVerificacao;
import br.com.papaprecoapi.repository.CodigoVerificacaoRepository;


@Service
public class CodigoVerificacaoService {
    /*@Autowired
    private UsuarioRepository usuarioRepository;

    public String gerarCodigoVerificarEmailUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString();
        } while (usuarioRepository.existsByCodigoVerificarEmail(codigo));
        return codigo;
    }*/
    @Autowired
    private CodigoVerificacaoRepository codigoVerificacaoRepository;

    public String gerarRandomAlphanumeric6Codigo() {
        return gerarRandomAlphanumericCodigo(6).toUpperCase();
    }

    public String gerarRandomAlphanumericCodigo(int tamanho) {
        return RandomStringUtils.randomAlphanumeric(tamanho);
    }

    public void salvar(CodigoVerificacao codigo) {
        codigoVerificacaoRepository.save(codigo);
    }

    public String validarCodigoVerificacao(String codigo, String email) {
        CodigoVerificacao c = codigoVerificacaoRepository.findByUsuario_emailAndCodigoIgnoreCase(email, codigo.replace("-", ""));
    
        return c == null ? "Código inválido!"
            : c.isCodigoExpirado() ? "Código expirado!" : null;
    }

    public List<CodigoVerificacao> findAllByEmailHoje(String email) {
        return codigoVerificacaoRepository.findAllByEmailHoje(email);
    }

    public CodigoVerificacao findByCodigoAndUsuario_Email(String codigo, String email) {
        return codigoVerificacaoRepository.findByUsuario_emailAndCodigoIgnoreCase(email, codigo.replace("-", ""));
    }
}
