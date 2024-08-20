package br.com.premiumpriceapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.premiumpriceapi.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto,Integer> {
    public List<Produto> findByNomeContainingIgnoringCase(String nome);

    public List<Produto> findByNomeContainingIgnoreCaseAndPrecoBetween(String nome, Double precoMin, Double precoMax);

    @Query(value = """
        WITH RankedProducts AS (
            SELECT 
                p.id,
                p.nome,
                p.descricao,
                p.preco,
                p.latitude,
                p.longitude,
                p.data_insercao,
                CASE 
                    WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                    ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
                END AS tt,
                COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) AS votos_up,
                COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0) AS votos_down,
                levenshtein(LOWER(p.nome), LOWER(:palavra)) AS levenshtein_distance,
                ROW_NUMBER() OVER (
                    PARTITION BY LOWER(p.nome), p.latitude, p.longitude
                    ORDER BY 
                        levenshtein(LOWER(p.nome), LOWER(:palavra)),
                        CASE 
                            WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                            ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
                        END DESC,
                        p.data_insercao DESC
                ) AS rn
            FROM produto p
            LEFT JOIN voto_usuario_produto v ON p.id = v.id_produto
            WHERE p.preco BETWEEN :precoMin AND :precoMax
            GROUP BY p.id/*, LOWER(p.nome), p.latitude, p.longitude, p.data_insercao*/
        )
        SELECT 
            id,
            nome,
            descricao,
            preco,
            latitude,
            longitude,
            data_insercao,
            tt,
            votos_up,
            votos_down,
            levenshtein_distance
        FROM RankedProducts
        WHERE rn = 1
        ORDER BY 
            levenshtein_distance,
            tt DESC,
            data_insercao DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Produto> buscarProdutosPorPalavraEPrecoRanking(@Param("palavra") String palavra, @Param("precoMin") Double precoMin, @Param("precoMax") Double precoMax);
    
    @Query(value = """
        SELECT 
            p.id,
            p.nome,
            p.descricao,
            p.preco,
            p.latitude,
            p.longitude,
            p.data_insercao,
            CASE 
                WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
            END AS tt,
            COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) AS votos_up,
            COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0) AS votos_down
        FROM produto p
        LEFT JOIN voto_usuario_produto v ON p.id = v.id_produto
        WHERE LOWER(p.nome) = LOWER(:nome) AND p.latitude = :latitude AND p.longitude = :longitude
        GROUP BY p.id
        ORDER BY
            CASE 
                WHEN (COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END), 0)) >= 0 THEN 1
                ELSE COALESCE(SUM(CASE WHEN v.voto = TRUE THEN 1 ELSE 0 END)) - COALESCE(SUM(CASE WHEN v.voto = FALSE THEN 1 ELSE 0 END))
            END DESC,
            p.data_insercao DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Produto> buscarHistoricoProdutoRanking(@Param("nome") String nome, @Param("latitude") Double latitude, @Param("longitude") Double longitude);
}
