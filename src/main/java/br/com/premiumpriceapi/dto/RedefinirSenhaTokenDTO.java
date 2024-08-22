package br.com.premiumpriceapi.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedefinirSenhaTokenDTO {
    private Integer id;
    private String token;
    private UsuarioDTO usuario;
    private Date dataValidade;
    private String novaSenha;
}
