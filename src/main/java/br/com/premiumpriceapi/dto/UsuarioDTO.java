package br.com.premiumpriceapi.dto;

import java.util.Set;

import br.com.premiumpriceapi.model.VotoUsuarioProduto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Integer id;
    private String cpf;
    private String nome;
    private String sobrenome;
    private Set<VotoUsuarioProduto> votos;
}
