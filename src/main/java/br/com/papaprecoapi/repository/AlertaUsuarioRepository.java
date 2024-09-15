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
        WITH produtos_sem_voto_negativo AS (
            SELECT 
                p.id AS produto_id,
                p.nome,
                p.preco,
                p.data_observacao,
                l.latitude AS produto_latitude,
                l.longitude AS produto_longitude,
                SUM(CASE WHEN vup.voto = false THEN 1 ELSE 0 END) AS votos_negativos
            FROM 
                produto p
            JOIN 
                localizacao l 
                ON p.localizacao_id = l.id
            LEFT JOIN 
                voto_usuario_produto vup 
                ON vup.id_produto = p.id
            GROUP BY 
                p.id, l.latitude, l.longitude
            HAVING 
                SUM(CASE WHEN vup.voto = false THEN 1 ELSE 0 END) = 0
        ),
        localizacao_usuario AS (
            SELECT 
                u.id AS usuario_id,
                l.latitude AS usuario_latitude,
                l.longitude AS usuario_longitude
            FROM 
                usuario u
            JOIN 
                localizacao l 
                ON u.localizacao_id = l.id
        )
        SELECT DISTINCT ON (au.usuario_id)
            au.id AS alerta_id,
            au.produto AS produto_alerta,
            au.preco AS preco_alerta,
            p.produto_id,
            p.nome AS nome_produto,
            p.preco AS preco_produto,
            p.data_observacao,
            u.fcm_token
        FROM 
            alerta_usuario au
        JOIN 
            produtos_sem_voto_negativo p 
            ON LOWER(p.nome) = LOWER(au.produto)
        JOIN 
            localizacao_usuario lu 
            ON haversine(lu.usuario_latitude, lu.usuario_longitude, p.produto_latitude, p.produto_longitude) <= 10
        JOIN usuario u ON u.id = au.usuario_id
        WHERE 
            p.preco <= au.preco AND u.fcm_token IS NOT NULL
        ORDER BY 
            au.usuario_id, 
            p.data_observacao DESC
    """, nativeQuery = true)
    List<AlertaProdutoDTO> findAlertasComProdutosValidos();
}
