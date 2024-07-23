package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.Loja;

public interface LojaRepository extends JpaRepository<Loja,Integer> {
    
}
