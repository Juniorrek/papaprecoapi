package br.com.premiumpriceapi.rest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.premiumpriceapi.dto.UsuarioDTO;
import br.com.premiumpriceapi.model.Usuario;
import br.com.premiumpriceapi.repository.UsuarioRepository;

@CrossOrigin
@RestController
@RequestMapping("usuarios")
public class UsuarioREST {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping(value = "/{id}" , produces = "application/json;charset=UTF-8")
    public UsuarioDTO buscarPorId(@PathVariable("id") Integer id){

        Usuario usuario = repo.findById(id).get();     

        return mapper.map(usuario, UsuarioDTO.class);
    }

    /*@PutMapping("/alterarSenha")
    public ResponseEntity<?> alterarSenha(@RequestBody AlterarSenhaRequestDTO alterarSenhaRequestDTO, HttpServletRequest request) {
        String token = tokenService.resolveToken(request);
        String senhaNova = alterarSenhaRequestDTO.getSenhaNova();
        String senhaAtual = alterarSenhaRequestDTO.getSenhaAtual();
        Integer usuarioId = alterarSenhaRequestDTO.getUsuarioId();

        if (token != null && senhaNova != null && senhaAtual != null && usuarioId != null) {
            String email = tokenService.validateToken(token);
            Usuario uToken = repo.findByEmail(email).get();

            Usuario usuario = repo.findById(usuarioId).get();
            
            if (uToken == null || usuario == null) return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");

            if (!uToken.getId().equals(usuario.getId())) return ResponseEntity.badRequest().body("Error: Não autorizado!");

            if (!encoder.matches(senhaAtual, usuario.getSenha())) return ResponseEntity.badRequest().body("Senha atual inválida!");

            usuario.setSenha(encoder.encode(senhaNova));
            repo.save(usuario);

            usuario.setSenha(null);
            return ResponseEntity.ok(mapper.map(usuario, UsuarioDTO.class));

        }

        return ResponseEntity.badRequest().body("Erro ao alterar senha");
    }*/

}
