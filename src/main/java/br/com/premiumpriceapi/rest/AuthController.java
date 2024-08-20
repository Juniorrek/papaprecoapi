package br.com.premiumpriceapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.premiumpriceapi.dto.request.AuthRequestDTO;
import br.com.premiumpriceapi.dto.request.RegisterUsuarioRequestDTO;
import br.com.premiumpriceapi.model.Usuario;
import br.com.premiumpriceapi.repository.UsuarioRepository;


@CrossOrigin
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("signin")
    public ResponseEntity login(@RequestBody AuthRequestDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.senha()));



        return ResponseEntity.ok().build();
    }

    @PostMapping("signup")
    public ResponseEntity<?> register(@RequestBody RegisterUsuarioRequestDTO authRequest) {
        if (usuarioRepo.existsByEmail(authRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body("Error: Email is already taken!");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(authRequest.getNome());
        usuario.setEmail(authRequest.getEmail());
        usuario.setSenha(encoder.encode(authRequest.getSenha()));

        usuario = usuarioRepo.save(usuario);

        return ResponseEntity.ok(usuario);
    }
    
}
