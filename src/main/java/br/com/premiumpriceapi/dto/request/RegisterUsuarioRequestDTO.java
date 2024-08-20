package br.com.premiumpriceapi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUsuarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
}
