package br.com.papaprecoapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VotoUsuarioProdutoDTO {
    private Long id;
    private Integer produtoId;
    private Integer usuarioId;
    private Boolean voto;
}
