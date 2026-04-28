package hubhds.bpo.service.esqueciSenha;

import hubhds.bpo.model.esqueciSenha.EsqueciSenha;
import hubhds.bpo.repository.esqueciSenha.EsqueciSenhaRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.service.email.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class EsqueciSenhaService {

    private final EsqueciSenhaRepository esqueciSenhaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.front-url}")
    private String frontUrl;

    public EsqueciSenhaService(
            EsqueciSenhaRepository esqueciSenhaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.esqueciSenhaRepository = esqueciSenhaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public String solicitarRecuperacao(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("E-mail é obrigatório");
        }

        String emailTratado = email.trim().toLowerCase();

        var usuarioOptional = usuarioRepository.findByEmail(emailTratado);

        if (usuarioOptional.isEmpty()) {
            return "Iremos enviar o link pelo e-mail informado";
        }

        String token = UUID.randomUUID().toString();

        EsqueciSenha esqueciSenha = EsqueciSenha.builder()
                .token(token)
                .email(emailTratado)
                .expiracao(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .criadoEm(LocalDateTime.now())
                .build();

        esqueciSenhaRepository.save(esqueciSenha);

        String link = frontUrl + "?token=" + token;

        emailService.enviarRedefinicaoSenha(emailTratado, link);

        return "Iremos enviar o link pelo e-mail informado";
    }

    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token é obrigatório");
        }

        if (novaSenha == null || novaSenha.isBlank()) {
            throw new RuntimeException("Nova senha é obrigatória.");
        }

        if (novaSenha.length() < 8) {
            throw new RuntimeException("A nova senha deve conter no mínimo 8 caracteres");
        }

        EsqueciSenha esqueciSenha = esqueciSenhaRepository.findByToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (Boolean.TRUE.equals(esqueciSenha.getUsado())) {
            throw new RuntimeException("Token já utilizado");
        }

        if (esqueciSenha.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        var usuario = usuarioRepository.findByEmail(esqueciSenha.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        esqueciSenha.setUsado(true);
        esqueciSenhaRepository.save(esqueciSenha);
    }
}
