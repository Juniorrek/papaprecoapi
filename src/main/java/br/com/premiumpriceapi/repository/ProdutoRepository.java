package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto,Integer> {
    
}
