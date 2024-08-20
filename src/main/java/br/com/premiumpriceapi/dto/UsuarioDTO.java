package br.com.premiumpriceapi.dto;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private Set<VotoUsuarioProdutoDTO> votos;
}
