package br.com.premiumpriceapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto,Integer> {
    public List<Produto> findByNomeContainingIgnoringCase(String nome);

    public List<Produto> findByNomeContainingIgnoreCaseAndPrecoBetween(String nome, Double precoMin, Double precoMax);
}
