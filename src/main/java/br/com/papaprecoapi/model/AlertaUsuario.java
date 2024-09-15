package br.com.papaprecoapi.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="alerta_usuario")
@Getter
@Setter
public class AlertaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String produto;

    private BigDecimal preco;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
