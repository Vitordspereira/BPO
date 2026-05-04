package hubhds.bpo.service.preCadastro;

import hubhds.bpo.dto.usuario.UsuarioCompletaCadastro;
import hubhds.bpo.dto.usuario.UsuarioResponse;
import hubhds.bpo.model.preCadastro.PreCadastro;
import hubhds.bpo.model.usuario.AmbienteUsuario;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.preCadastro.PreCadastroRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PreCadastroService {

    private final PreCadastroRepository preCadastroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PreCadastroService(
            PreCadastroRepository preCadastroRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.preCadastroRepository = preCadastroRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse finalizar(UsuarioCompletaCadastro request) {
        String token = request.token().trim();

        PreCadastro preCadastro = preCadastroRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Pré-cadastro não encontrado."));

        if (Boolean.TRUE.equals(preCadastro.getUsado())) {
            throw new RuntimeException("Este pré-cadastro já foi utilizado.");
        }

        if (preCadastro.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este pré-cadastro expirou.");
        }

        if (!"authorized".equalsIgnoreCase(preCadastro.getMpStatus())) {
            throw new RuntimeException("Assinatura ainda não está autorizada.");
        }

        String emailTratado = request.email().trim().toLowerCase();
        String telefoneLimpo = limparTelefone(request.telefone());
        String cpfLimpo = limparDocumento(request.cpf());

        usuarioRepository.findByEmail(emailTratado).ifPresent(usuario -> {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        });

        usuarioRepository.findByTelefone(telefoneLimpo).ifPresent(usuario -> {
            throw new RuntimeException("Este telefone já está cadastrado.");
        });

        Usuario usuario = Usuario.builder()
                .nomeCompleto(request.nomeCompleto().trim())
                .email(emailTratado)
                .telefone(telefoneLimpo)
                .cpf(cpfLimpo)
                .senha(passwordEncoder.encode(request.senha()))
                .temEmpresa(0)
                .assinaturaAtiva(true)
                .ambienteUsuario(AmbienteUsuario.PESSOAL)
                .mpPreapprovalId(preCadastro.getMpPreapprovalId())
                .mpExternalReference(preCadastro.getMpExternalReference())
                .mpStatus(preCadastro.getMpStatus())
                .mpAssinaturaAtualizadaEm(LocalDateTime.now())
                .tipoPlano(preCadastro.getPlano())
                .valorPlano(preCadastro.getValorPlano())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);

        preCadastro.setUsado(true);
        preCadastro.setAtualizadoEm(LocalDateTime.now());
        preCadastroRepository.save(preCadastro);

        return new UsuarioResponse(
                salvo.getIdUsuario(),
                salvo.getNomeCompleto(),
                salvo.getEmail(),
                salvo.getTelefone(),
                salvo.getAssinaturaAtiva()
        );
    }

    private String limparTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }

        return telefone.replaceAll("\\D", "");
    }

    private String limparDocumento(String documento) {
        if (documento == null) {
            return "";
        }

        return documento.replaceAll("\\D", "");
    }
}

