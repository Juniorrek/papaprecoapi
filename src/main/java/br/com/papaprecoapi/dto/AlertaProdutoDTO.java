package br.com.papaprecoapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AlertaProdutoDTO {
    Long getAlertaId();
    String getProdutoAlerta();
    BigDecimal getPrecoAlerta();
    Long getProdutoId();
    String getNomeProduto();
    BigDecimal getPrecoProduto();
    LocalDate getDataObservacao();
    String getFcmToken();
}
