package br.com.premiumpriceapi.rest;

import java.net.URI;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.auth.oauth2.TokenVerifier;

import br.com.premiumpriceapi.dto.RedefinirSenhaTokenDTO;
import br.com.premiumpriceapi.dto.request.GoogleLoginRequestDTO;
import br.com.premiumpriceapi.dto.request.LoginRequestDTO;
import br.com.premiumpriceapi.dto.request.RegisterUsuarioRequestDTO;
import br.com.premiumpriceapi.dto.response.LoginResponseDTO;
import br.com.premiumpriceapi.dto.response.LoginUsuarioResponseDTO;
import br.com.premiumpriceapi.model.RedefinirSenhaToken;
import br.com.premiumpriceapi.model.Usuario;
import br.com.premiumpriceapi.repository.RedefinirSenhaTokenRepository;
import br.com.premiumpriceapi.repository.UsuarioRepository;
import br.com.premiumpriceapi.services.EmailService;
import br.com.premiumpriceapi.services.JwtService;
import br.com.premiumpriceapi.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;


@CrossOrigin
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedefinirSenhaTokenRepository senhaTokenRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtService jwtService;

    private final String googleClientId = "736661748519-433ei1nefrp6m1f0k3forqbh904r8oac.apps.googleusercontent.com";

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        
        Optional<Usuario> usuario = usuarioRepo.findByEmail(loginRequestDTO.email());
        if(usuario.isEmpty() || usuario.get().getSenha() == null) {//senha null cadastrado google
            //throw new BadCredentialsException("Usuário não encontrado!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Usuário não encontrado!");
        }
        if (!passwordEncoder.matches(loginRequestDTO.senha(), usuario.get().getSenha())) {
            return ResponseEntity.badRequest().body("Email ou senha inválidos!");
        }

        String jwtToken = jwtService.generateToken(usuario.get().getEmail());
        
        return ResponseEntity.ok(
            new LoginResponseDTO(jwtToken, 
                new LoginUsuarioResponseDTO(
                    usuario.get().getId(),
                    usuario.get().getEmail(),
                    usuario.get().getNome(),
                    usuario.get().getVerificado())));
    }

    @PostMapping("/login/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequestDTO request) {
        //TokenVerifier tokenVerifier = TokenVerifier.newBuilder().build();
        /*System.out.println("--------------------------");
        System.out.println(request.idToken());
        System.out.println("--------------------------");*/
        TokenVerifier tokenVerifier = TokenVerifier.newBuilder()
            .setAudience(googleClientId)
            //.setIssuer("issuer-to-verify")
            .build();
        try {
            JsonWebSignature jsonWebSignature = tokenVerifier.verify(request.idToken());
            /*System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            System.out.println(jsonWebSignature.getPayload().get("additional-claim"));*/

            String email = jsonWebSignature.getPayload().get("email").toString();
            String name = jsonWebSignature.getPayload().get("name").toString();

            if (email == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Usuário não encontrado!");
            }
            Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
            Usuario u;
            // Criar usuário se não existir
            if (usuario.isEmpty()) {
                u = new Usuario();
                u.setEmail(email);
                u.setNome(name);
                u.setVerificado(true);
                u = usuarioRepo.save(u);
            } else {
                u = usuario.get();
            }

            String jwtToken = jwtService.generateToken(usuario.get().getEmail());


            return ResponseEntity.ok(
                new LoginResponseDTO(jwtToken,
                    new LoginUsuarioResponseDTO(
                        u.getId(),
                        u.getEmail(),
                        u.getNome(),
                        true)));
        } catch (TokenVerifier.VerificationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao autenticar via Google!");
        }
    }

    /*@PostMapping("signin")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.senha()));

        String token = jwtService.generateJwtToken((Usuario) authentication.getPrincipal());

        Optional<Usuario> usuario = usuarioRepo.findByEmail(authRequest.email());
        if(usuario.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        LoginUsuarioResponseDTO u = new LoginUsuarioResponseDTO(usuario.get().getId(), usuario.get().getEmail(), usuario.get().getNome());

        return ResponseEntity.ok(new LoginResponseDTO(token, 1L));
    }*/

    @PostMapping("signup")
    public ResponseEntity<?> register(@RequestBody RegisterUsuarioRequestDTO authRequest) {
        Optional<Usuario> u = usuarioRepo.findByEmail(authRequest.getEmail());
        boolean existsByEmail = usuarioRepo.existsByEmail(authRequest.getEmail());
        if (existsByEmail
            && u.get().getSenha() != null) {
            return ResponseEntity.badRequest()
                .body("Error: Email já cadastrado!");
        }

        Usuario usuario;
        if (!existsByEmail) {
            usuario = new Usuario();
            usuario.setNome(authRequest.getNome());
            usuario.setEmail(authRequest.getEmail());
            usuario.setSenha(passwordEncoder.encode(authRequest.getSenha()));
            usuario.setVerificado(false);
            String token = tokenService.gerarTokenVerificarEmailUnico();
            usuario.setTokenVerificarEmail(token);

            enviarEmailCadastro(usuario);
        } else {
            usuario = u.get();
            usuario.setNome(authRequest.getNome());
            usuario.setEmail(authRequest.getEmail());
            usuario.setSenha(passwordEncoder.encode(authRequest.getSenha()));
        }
        usuario = usuarioRepo.save(usuario);


        return ResponseEntity.ok(new LoginUsuarioResponseDTO(usuario.getId(), usuario.getEmail(), usuario.getNome(), usuario.getVerificado()));
    }

    @GetMapping(value = "verificarEmail", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView verificarEmail(@PathParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView("verificar-email-page");
        Usuario u = usuarioRepo.findByTokenVerificarEmail(token);
        if (u == null) {
            modelAndView.addObject("sucesso", false);
        } else {
            u.setVerificado(true);
            //u.setTokenVerificarEmail(null);
            usuarioRepo.save(u);
            modelAndView.addObject("sucesso", true);
        }

        return modelAndView;
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

        enviarEmailRedefinirSenhaToken(token, usuario.get());

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
            rsToken.getUsuario().setSenha(passwordEncoder.encode(senhaDto.getNovaSenha()));
            usuarioRepo.save(rsToken.getUsuario());

            rsToken.setDataValidade(new Date());
            senhaTokenRepo.save(rsToken);

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

    private void enviarEmailCadastro(Usuario usuario) {
        URI currentUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build()
                .toUri();

        String msg = "Cadastrado com sucesso \r\n" + 
                    "Para verificar acesse o link abaixo: \r\n" +
                    "http://" + currentUri.getAuthority() + "/premiumpriceapi/auth/verificarEmail?token=" + usuario.getTokenVerificarEmail();

        emailService.sendEmail(usuario.getEmail(), "Premium Price - Verificação de email", msg);
    }

    private void enviarEmailRedefinirSenhaToken(String token, Usuario usuario) {
        String msg = "Token \r\n" + token;

        emailService.sendEmail(usuario.getEmail(), "Redefinir Senha", msg);
    }
}
