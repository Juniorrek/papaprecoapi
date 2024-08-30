package br.com.papaprecoapi.rest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.papaprecoapi.dto.UsuarioDTO;
import br.com.papaprecoapi.dto.request.AlterarSenhaRequestDTO;
import br.com.papaprecoapi.model.Usuario;
import br.com.papaprecoapi.repository.UsuarioRepository;

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

    @PutMapping("/alterarSenha")
    public ResponseEntity<?> alterarSenha(@RequestBody AlterarSenhaRequestDTO alterarSenhaRequestDTO) {
        String senhaNova = alterarSenhaRequestDTO.getSenhaNova();
        String senhaAtual = alterarSenhaRequestDTO.getSenhaAtual();
        Integer usuarioId = alterarSenhaRequestDTO.getUsuarioId();

        if (senhaNova != null && senhaAtual != null && usuarioId != null) {
            Usuario usuario = repo.findById(usuarioId).get();
            
            if (usuario == null) return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");

            //if (!uToken.getId().equals(usuario.getId())) return ResponseEntity.badRequest().body("Error: Não autorizado!");

            if (!encoder.matches(senhaAtual, usuario.getSenha())) return ResponseEntity.badRequest().body("Senha atual inválida!");

            usuario.setSenha(encoder.encode(senhaNova));
            repo.save(usuario);

            usuario.setSenha(null);
            return ResponseEntity.ok(mapper.map(usuario, UsuarioDTO.class));

        }

        return ResponseEntity.badRequest().body("Erro ao alterar senha");
    }

}
