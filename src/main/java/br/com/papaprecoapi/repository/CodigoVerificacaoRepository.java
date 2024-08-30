package br.com.papaprecoapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.papaprecoapi.model.CodigoVerificacao;

@Repository
public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao,Integer> {
    CodigoVerificacao findByUsuario_emailAndCodigoIgnoreCase(String email, String codigo);
    
    @Query("SELECT cv FROM CodigoVerificacao cv " +
           "JOIN cv.usuario u " +
           "WHERE FUNCTION('DATE', cv.dataGeracao) = CURRENT_DATE " +
           "AND u.email = :email")
    List<CodigoVerificacao> findAllByEmailHoje(@Param("email") String email);
}
