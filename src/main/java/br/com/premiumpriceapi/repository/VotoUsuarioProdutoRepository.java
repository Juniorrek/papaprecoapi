package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.VotoUsuarioProduto;

public interface VotoUsuarioProdutoRepository extends JpaRepository<VotoUsuarioProduto,Integer> {
    VotoUsuarioProduto findByUsuario_IdAndProduto_Id(Integer idUsuario, Integer idProduto);
}
