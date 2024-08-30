package br.com.papaprecoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.papaprecoapi.model.VotoUsuarioProduto;

@Repository
public interface VotoUsuarioProdutoRepository extends JpaRepository<VotoUsuarioProduto,Integer> {
    VotoUsuarioProduto findByUsuario_IdAndProduto_Id(Integer idUsuario, Integer idProduto);
}
