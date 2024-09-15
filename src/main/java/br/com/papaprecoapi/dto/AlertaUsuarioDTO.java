package br.com.papaprecoapi.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertaUsuarioDTO {
    private Long id;
    private String produto;
    private BigDecimal preco;
    private UsuarioDTO usuario;
}
