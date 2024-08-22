package br.com.premiumpriceapi.rest;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.premiumpriceapi.dto.RedefinirSenhaTokenDTO;
import br.com.premiumpriceapi.dto.request.AuthRequestDTO;
import br.com.premiumpriceapi.dto.request.RegisterUsuarioRequestDTO;
import br.com.premiumpriceapi.dto.response.LoginResponseDTO;
import br.com.premiumpriceapi.model.RedefinirSenhaToken;
import br.com.premiumpriceapi.model.Usuario;
import br.com.premiumpriceapi.repository.RedefinirSenhaTokenRepository;
import br.com.premiumpriceapi.repository.UsuarioRepository;
import br.com.premiumpriceapi.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;


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

    @Autowired
    private JwtService tokenService;

    @Autowired
    private RedefinirSenhaTokenRepository senhaTokenRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @PostMapping("signin")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.senha()));

        String token = tokenService.generateJwtToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("signup")
    public ResponseEntity<?> register(@RequestBody RegisterUsuarioRequestDTO authRequest) {
        if (usuarioRepo.existsByEmail(authRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body("Error: Email já cadastrado!");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(authRequest.getNome());
        usuario.setEmail(authRequest.getEmail());
        usuario.setSenha(encoder.encode(authRequest.getSenha()));

        usuario = usuarioRepo.save(usuario);

        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/redefinirSenha/gerarToken")
    public ResponseEntity<?> redefinirSenhaGerarToken(HttpServletRequest request, @RequestParam("email") String email) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        String token = RandomStringUtils.randomAlphanumeric(4);
        
        RedefinirSenhaToken t = new RedefinirSenhaToken(token, usuario.get());
        senhaTokenRepo.save(t);

        mailSender.send(emailRedefinirSenhaToken(token, usuario.get()));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/redefinirSenha/validarToken")
    public ResponseEntity<?> redefinirSenhaValidarToken(@RequestParam("email") String email, @RequestParam("token") String token) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        String msgErro = validarRedefinirSenhaToken(token, usuario.get().getEmail());
        if(msgErro != null) {
            return ResponseEntity.badRequest().body(msgErro);
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/redefinirSenha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaTokenDTO senhaDto) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(senhaDto.getUsuario().getEmail());
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        String msgErro = validarRedefinirSenhaToken(senhaDto.getToken(), senhaDto.getUsuario().getEmail());
        if(msgErro != null) {
            return ResponseEntity.badRequest().body(msgErro);
        }

        RedefinirSenhaToken rsToken = senhaTokenRepo.findByTokenAndUsuario_email(senhaDto.getToken(), senhaDto.getUsuario().getEmail());
        if(rsToken != null && rsToken.getUsuario() != null) {
            rsToken.getUsuario().setSenha(encoder.encode(senhaDto.getNovaSenha()));
            usuarioRepo.save(rsToken.getUsuario());

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Erro ao redefinir senha");
        }
    }

    public String validarRedefinirSenhaToken(String token, String email) {
        RedefinirSenhaToken rsToken = senhaTokenRepo.findByTokenAndUsuario_email(token, email);
    
        return rsToken == null ? "Token invalido!"
            : rsToken.isTokenExpirado() ? "Token expirado!" : null;
    }

    private SimpleMailMessage emailRedefinirSenhaToken(String token, Usuario usuario) {
        String msg = "Token \r\n" + token;

        return construirEmail("Redefinir Senha", msg, usuario.getEmail());
    }

    private SimpleMailMessage construirEmail(String subject, String text, String to) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(text);
        email.setTo(to);
        email.setFrom(env.getProperty("spring.mail.username"));
        return email;
    }
}
