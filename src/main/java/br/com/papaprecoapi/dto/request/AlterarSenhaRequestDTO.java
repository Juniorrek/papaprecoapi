package br.com.papaprecoapi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarSenhaRequestDTO {
    private Integer usuarioId;
    private String senhaNova;
    private String senhaAtual;
}
