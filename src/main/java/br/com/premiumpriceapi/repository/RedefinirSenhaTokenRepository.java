package br.com.premiumpriceapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.premiumpriceapi.model.RedefinirSenhaToken;

@Repository
public interface RedefinirSenhaTokenRepository extends JpaRepository<RedefinirSenhaToken,Integer> {
    RedefinirSenhaToken findByTokenAndUsuario_email(String token, String email);

}
