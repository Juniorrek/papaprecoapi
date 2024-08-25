package br.com.premiumpriceapi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="produto")
@Getter
@Setter
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacao;

    @Column(name = "data_insercao", insertable = false, updatable = false)
    private LocalDateTime dataInsercao;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VotoUsuarioProduto> votos;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    /*@Column(name = "votos_up", insertable = false, updatable = true)
    private Integer votosUp;
    @Column(name = "votos_down", insertable = false, updatable = true)
    private Integer votosDown;*/
}
