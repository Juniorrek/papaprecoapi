package br.com.papaprecoapi.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.auth.oauth2.TokenVerifier;

import br.com.papaprecoapi.dto.RedefinirSenhaCodigoDTO;
import br.com.papaprecoapi.dto.request.GoogleLoginRequestDTO;
import br.com.papaprecoapi.dto.request.LoginRequestDTO;
import br.com.papaprecoapi.dto.request.RegisterUsuarioRequestDTO;
import br.com.papaprecoapi.dto.response.LoginResponseDTO;
import br.com.papaprecoapi.dto.response.LoginUsuarioResponseDTO;
import br.com.papaprecoapi.model.CodigoVerificacao;
import br.com.papaprecoapi.model.CodigoVerificacao.TipoCodigoVerificacao;
import br.com.papaprecoapi.model.Usuario;
import br.com.papaprecoapi.repository.UsuarioRepository;
import br.com.papaprecoapi.services.CodigoVerificacaoService;
import br.com.papaprecoapi.services.EmailService;
import br.com.papaprecoapi.services.JwtService;


@CrossOrigin
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CodigoVerificacaoService codigoVerificacaoService;

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
        if (!usuario.get().isVerificado()) {
            return ResponseEntity.badRequest().body("Email não verificado!");
        }

        String jwtToken = jwtService.generateToken(usuario.get().getEmail());
        
        return ResponseEntity.ok(
            new LoginResponseDTO(jwtToken, 
                new LoginUsuarioResponseDTO(
                    usuario.get().getId(),
                    usuario.get().getEmail(),
                    usuario.get().getNome(),
                    usuario.get().isVerificado())));
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

            String jwtToken = jwtService.generateToken(u.getEmail());


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
            usuario = usuarioRepo.save(usuario);
        } else {
            usuario = u.get();
            usuario.setNome(authRequest.getNome());
            usuario.setEmail(authRequest.getEmail());
            usuario.setSenha(passwordEncoder.encode(authRequest.getSenha()));
            usuario = usuarioRepo.save(usuario);
        }


        return ResponseEntity.ok(new LoginUsuarioResponseDTO(usuario.getId(), usuario.getEmail(), usuario.getNome(), usuario.isVerificado()));
    }

    @GetMapping("/verificarEmail")
    public ResponseEntity<?> verificarEmail(@RequestParam("email") String email, @RequestParam("codigo") String codigo) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        String msgErro = codigoVerificacaoService.validarCodigoVerificacao(codigo, usuario.get().getEmail());
        if(msgErro != null) {
            return ResponseEntity.badRequest().body(msgErro);
        } else {
            usuario.get().setVerificado(true);
            usuarioRepo.save(usuario.get());
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/validarCodigoVerificacao")
    public ResponseEntity<?> redefinirSenhaValidarToken(@RequestParam("email") String email, @RequestParam("codigo") String codigo) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        String msgErro = codigoVerificacaoService.validarCodigoVerificacao(codigo, usuario.get().getEmail());
        if(msgErro != null) {
            return ResponseEntity.badRequest().body(msgErro);
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/enviarCodigoVerificacao")
    public ResponseEntity<?> enviarCodigoVerificacao(@RequestParam("email") String email, @RequestParam("tipo") String tipo) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        List<CodigoVerificacao> codigos = codigoVerificacaoService.findAllByEmailHoje(email);
        if(codigos != null && codigos.size() >= 10) {
            return ResponseEntity.badRequest().body("Error: Muito códigos enviados hoje, tente novamente amanhã!");
        }

        String codigo = codigoVerificacaoService.gerarRandomAlphanumeric6Codigo();
        CodigoVerificacao c = new CodigoVerificacao(codigo, usuario.get(), TipoCodigoVerificacao.valueOf(tipo));
        codigoVerificacaoService.salvar(c);

        enviarEmailCodigoVerificacao(usuario.get(), c);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/redefinirSenha/gerarToken")
    public ResponseEntity<?> redefinirSenhaGerarToken(@RequestParam("email") String email) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/redefinirSenha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaCodigoDTO rscDTO) {
        Optional<Usuario> usuario = usuarioRepo.findByEmail(rscDTO.getEmail());
        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuário não encontrado!");
        }

        CodigoVerificacao c = codigoVerificacaoService.findByCodigoAndUsuario_Email(rscDTO.getCodigo(), rscDTO.getEmail());
        if (c == null) {
            return ResponseEntity.badRequest().body("Error: Código não encontrado!");
        }
        if (c.isCodigoExpirado()) {
            return ResponseEntity.badRequest().body("Error: Código expirado!");
        }

        usuario.get().setSenha(passwordEncoder.encode(rscDTO.getNovaSenha()));
        usuarioRepo.save(usuario.get());

        return ResponseEntity.ok().build();
    }

    private void enviarEmailCodigoVerificacao(Usuario usuario, CodigoVerificacao c) {
        if(c.getTipo() == TipoCodigoVerificacao.VERIFICAR_EMAIL)
            enviarEmailVerificarEmail(usuario, c);

        if(c.getTipo() == TipoCodigoVerificacao.REDEFINIR_SENHA)
            enviarEmailRedefinirSenha(usuario, c);

    }

    private void enviarEmailVerificarEmail(Usuario usuario, CodigoVerificacao c) {
        /*URI currentUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build()
                .toUri();*/

        String msg = "Para verificar seu email digite o código abaixo no app: \r" +
                     "Código: " + c.getCodigo().substring(0, 3) + "-" + c.getCodigo().substring(3);

        emailService.sendEmail(usuario.getEmail(), "Papa Preço - Verificação de email", msg);
    }

    private void enviarEmailRedefinirSenha(Usuario usuario, CodigoVerificacao c) {
        String msg = "Para redefinir sua senha digite o código abaixo no app: \r" +
                     "Código: " + c.getCodigo().substring(0, 3) + "-" + c.getCodigo().substring(3);

        emailService.sendEmail(usuario.getEmail(), "Papa Preço - Redefinir Senha", msg);
    }
}
