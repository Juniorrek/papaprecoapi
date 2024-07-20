package br.com.premiumpriceapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private int id;
    private String cpf;
    private String nome;
    private String sobrenome;
}
