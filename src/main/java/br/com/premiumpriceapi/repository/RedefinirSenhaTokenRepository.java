package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.premiumpriceapi.model.RedefinirSenhaToken;

public interface RedefinirSenhaTokenRepository extends JpaRepository<RedefinirSenhaToken,Integer> {
    RedefinirSenhaToken findByTokenAndUsuario_email(String token, String email);

}
