package br.com.papaprecoapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdutoDTO {
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private LocalizacaoDTO localizacao;
    private LocalDateTime dataInsercao;
    private List<VotoUsuarioProdutoDTO> votos;
    /*private Integer votosUp;
    private Integer votosDown;*/
}
