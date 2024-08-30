package br.com.papaprecoapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalizacaoDTO {
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String descricao;
}
