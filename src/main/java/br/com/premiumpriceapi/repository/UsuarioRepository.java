package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    
}
