package br.com.papaprecoapi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private List<VotoUsuarioProdutoDTO> votos;
}
