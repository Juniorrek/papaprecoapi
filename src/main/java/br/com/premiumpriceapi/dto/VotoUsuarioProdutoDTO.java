package br.com.premiumpriceapi.dto;

import br.com.premiumpriceapi.model.Produto;
import br.com.premiumpriceapi.model.Usuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VotoUsuarioProdutoDTO {
    private Long id;
    private Produto produto;
    private Usuario usuario;
    private Boolean voto;
}
