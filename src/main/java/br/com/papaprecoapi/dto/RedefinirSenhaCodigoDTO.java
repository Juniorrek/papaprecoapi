package br.com.papaprecoapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedefinirSenhaCodigoDTO {
    private String codigo;
    private String email;
    private String novaSenha;
}
