package br.com.papaprecoapi.model;

import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodigoVerificacao {
    public CodigoVerificacao(String token, Usuario usuario, TipoCodigoVerificacao tipo) {
        this.codigo = token;
        this.usuario = usuario;

        this.dataValidade = gerarDataValidade(EXPIRATION);
        this.tipo = tipo;
    }

    private static final int EXPIRATION = 60 * 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
 
    private String codigo;
 
    @ManyToOne
    @JoinColumn(nullable = false, name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tipo", columnDefinition = "tipo_codigo_verificacao")
    @Enumerated(EnumType.STRING)
    private TipoCodigoVerificacao tipo;

    @Column(name = "data_validade")
    private Date dataValidade;

    @Column(name = "data_geracao", insertable = false, updatable = false)
    private Date dataGeracao;

    public boolean isCodigoExpirado() {
        final Calendar cal = Calendar.getInstance();
        return this.getDataValidade().before(cal.getTime());
    }

    private Date gerarDataValidade(int timeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, timeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public enum TipoCodigoVerificacao {
        VERIFICAR_EMAIL,
        REDEFINIR_SENHA
    }
}
