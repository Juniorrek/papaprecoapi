package br.com.papaprecoapi.model;

import java.util.List;

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
@Table(name="usuario")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;
    @Column
    private String senha;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotoUsuarioProduto> votos;

    @OneToMany(mappedBy = "usuario")
    private List<Produto> produtos;

    @ManyToOne
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacao;

    private boolean verificado;

    public boolean isVerificado() {
        return verificado;
    }

    @Column(name = "fcm_token")
    private String fcmToken;
}
