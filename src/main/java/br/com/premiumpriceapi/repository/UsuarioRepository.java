package br.com.premiumpriceapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.premiumpriceapi.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {
    Optional<Usuario> findByEmail(String email);
    Usuario findByEmailAndSenha(String email, String senha);
    boolean existsByEmail(String email);
    boolean existsByTokenVerificarEmail(String token);
    Usuario findByTokenVerificarEmail(String token);
}
