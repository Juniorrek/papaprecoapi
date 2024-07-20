package br.com.premiumpriceapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutoDTO {
    private int id;
    private String nome;
    private String descricao;
    private String preco;
}
