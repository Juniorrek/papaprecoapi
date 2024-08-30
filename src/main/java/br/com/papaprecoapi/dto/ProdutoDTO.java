package br.com.papaprecoapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoDTO {
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private LocalizacaoDTO localizacao;
    private LocalDateTime dataInsercao;
    private LocalDateTime dataObservacao;
    private List<VotoUsuarioProdutoDTO> votos;
    private UsuarioDTO usuario;

    private double distanciaRelativa;
    private String dataRelativa;
    /*private Integer votosUp;
    private Integer votosDown;*/
}
