package br.com.premiumpriceapi.model;

import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class RedefinirSenhaToken {
    public RedefinirSenhaToken(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;

        this.dataValidade = gerarDataValidade(EXPIRATION);
    }

    private static final int EXPIRATION = 60 * 1;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
 
    private String token;
 
    @ManyToOne
    @JoinColumn(nullable = false, name = "usuario_id")
    private Usuario usuario;
 
    @Column(name = "data_validade")
    private Date dataValidade;

    public boolean isTokenExpirado() {
        final Calendar cal = Calendar.getInstance();
        return this.getDataValidade().before(cal.getTime());
    }

    private Date gerarDataValidade(int timeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, timeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
