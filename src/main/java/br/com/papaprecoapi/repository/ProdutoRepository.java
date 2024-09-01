package br.com.papaprecoapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.papaprecoapi.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    public List<Produto> findByNomeContainingIgnoringCase(String nome);

    public List<Produto> findByNomeContainingIgnoreCaseAndPrecoBetween(String nome, Double precoMin, Double precoMax);

    @Query(value = "WITH RankedProducts AS ( " +
            "SELECT " +
            "p.id, " +
            "p.nome, " +
            "p.descricao, " +
            "p.preco, " +
            "l.latitude, " +
            "l.longitude, " +
            "p.data_insercao, " +
            "p.data_observacao, " +
            "p.usuario_id, " +
            "p.localizacao_id, " +
            "CASE " +
            "WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1 "
            +
            "ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END)) "
            +
            "END AS tt, " +
            "COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) AS votos_up, " +
            "COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0) AS votos_down, " +
            "similarity(LOWER(p.nome), LOWER(:palavra)) AS similarity_score, " +
            "ROW_NUMBER() OVER ( " +
            "PARTITION BY LOWER(p.nome), l.latitude, l.longitude " +
            "ORDER BY " +
            "similarity(LOWER(p.nome), LOWER(:palavra)) DESC, " +
            "CASE " +
            "WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1 "
            +
            "ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END)) "
            +
            "END DESC, " +
            "p.data_observacao DESC, " +
            "p.data_insercao DESC " +
            ") AS rn " +
            "FROM produto p " +
            "JOIN localizacao l ON l.id = p.localizacao_id " +
            "LEFT JOIN voto_usuario_produto v ON p.id = v.id_produto " +
            "WHERE p.preco BETWEEN :precoMin AND :precoMax " +
            "GROUP BY p.id, l.latitude, l.longitude/*, LOWER(p.nome), p.latitude, p.longitude, p.data_observacao*/ " +
            ") " +
            "SELECT " +
            "id, " +
            "nome, " +
            "descricao, " +
            "preco, " +
            "latitude, " +
            "longitude, " +
            "data_insercao, " +
            "data_observacao, " +
            "usuario_id, " +
            "localizacao_id, " +
            "tt, " +
            "votos_up, " +
            "votos_down, " +
            "similarity_score " +
            "FROM RankedProducts " +
            "WHERE rn = 1 " +
            "ORDER BY " +
            "similarity_score DESC, " +
            "tt DESC, " +
            "data_observacao DESC, " +
            "data_insercao DESC " +
            "LIMIT 10 ", nativeQuery = true)
    List<Produto> buscarProdutosPorPalavraEPrecoRanking(@Param("palavra") String palavra,
            @Param("precoMin") Double precoMin, @Param("precoMax") Double precoMax);

    @Query(value = """
            SELECT
            p.id,
            p.nome,
            p.descricao,
            p.preco,
            l.latitude,
            l.longitude,
            p.data_insercao,
            p.data_observacao,
            p.usuario_id,
            p.localizacao_id,
            CASE
                WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
            END AS tt,
            COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) AS votos_up,
            COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0) AS votos_down
            FROM produto p
            JOIN localizacao l ON l.id = p.localizacao_id
            LEFT JOIN voto_usuario_produto v ON p.id = v.id_produto
            WHERE LOWER(p.nome) = LOWER(:nome) AND l.latitude = :latitude AND l.longitude = :longitude
            GROUP BY p.id, l.latitude, l.longitude
            ORDER BY
            CASE
                WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
            END DESC,
            p.data_observacao DESC,
            p.data_insercao DESC
            LIMIT 10
                        """, nativeQuery = true)
    List<Produto> buscarHistoricoProdutoRanking(@Param("nome") String nome, @Param("latitude") Double latitude,
            @Param("longitude") Double longitude);
}
