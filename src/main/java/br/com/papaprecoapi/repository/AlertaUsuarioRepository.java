package br.com.papaprecoapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.papaprecoapi.dto.AlertaProdutoDTO;
import br.com.papaprecoapi.model.AlertaUsuario;

@Repository
public interface AlertaUsuarioRepository extends JpaRepository<AlertaUsuario,Integer> {
    List<AlertaUsuario> findByUsuario_id(Integer usuarioId);

    @Query(value = """
        SELECT DISTINCT ON (u.id)
            u.id AS usuario_id,
            au.id AS alerta_id,
            au.produto AS produto_alerta,
            au.preco AS preco_alerta,
            p.produto_id,
            p.nome AS nome_produto,
            p.preco AS preco_produto,
            p.data_observacao,
            u.fcm_token
        FROM alerta_aleatorio aa
        JOIN alerta_usuario au ON au.id = aa.alerta_id
        JOIN usuario u ON u.id = au.usuario_id     
        JOIN produtos_sem_voto_negativo p
            ON similarity(LOWER(au.produto), LOWER(p.nome)) > 0.3
        JOIN localizacao lu 
            ON lu.id = u.localizacao_id AND
            haversine(lu.latitude, lu.longitude, p.produto_latitude, p.produto_longitude) <= 10  
        WHERE p.preco <= au.preco AND u.fcm_token IS NOT NULL
        AND aa.rn = 1
        ORDER BY u.id
    """, nativeQuery = true)
    List<AlertaProdutoDTO> findAlertasComProdutosValidos();
}
