package hubhds.bpo.service.usuario;

import hubhds.bpo.dto.usuario.UsuarioCompletaCadastro;
import hubhds.bpo.dto.usuario.UsuarioRequest;
import hubhds.bpo.dto.usuario.UsuarioResponse;
import hubhds.bpo.dto.usuario.perfil.AmbientesDTO;
import hubhds.bpo.dto.usuario.perfil.PerfilDTO;
import hubhds.bpo.model.usuario.AmbienteUsuario;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import hubhds.bpo.dto.usuario.perfil.AlterarAmbienteDTO;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastroManual(UsuarioRequest usuarioRequest) {
        return finalizarCadastroPosPagamento(
                usuarioRequest.nomeCompleto(),
                usuarioRequest.email(),
                usuarioRequest.telefone(),
                usuarioRequest.senha()
        );
    }

    @Transactional
    public UsuarioResponse completarCadastroPosPagamento(UsuarioCompletaCadastro usuarioCompletaCadastro) {
        return finalizarCadastroPosPagamento(
                usuarioCompletaCadastro.nomeCompleto(),
                usuarioCompletaCadastro.email(),
                usuarioCompletaCadastro.telefone(),
                usuarioCompletaCadastro.senha()
        );
    }

    private UsuarioResponse finalizarCadastroPosPagamento(
            String nomeCompleto,
            String email,
            String telefone,
            String senha
    ) {
        String telefoneLimpo = limparTelefone(telefone);

        if (telefoneLimpo == null || telefoneLimpo.isBlank()) {
            throw new RuntimeException("Telefone é obrigatório.");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("E-mail é obrigatório.");
        }

        if (senha == null || senha.isBlank()) {
            throw new RuntimeException("Senha é obrigatória.");
        }

        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new RuntimeException("Nome completo é obrigatório.");
        }

        String emailTratado = email.trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByTelefone(telefoneLimpo)
                .orElseThrow(() -> new RuntimeException(
                        "Não encontramos uma assinatura vinculada a este telefone."
                ));

        boolean assinaturaValida = Boolean.TRUE.equals(usuario.getAssinaturaAtiva())
                || "authorized".equalsIgnoreCase(usuario.getMpStatus());

        if (!assinaturaValida) {
            throw new RuntimeException(
                    "Cadastro não permitido: sua assinatura ainda não está ativa."
            );
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            throw new RuntimeException("Este telefone já possui cadastro finalizado.");
        }

        usuarioRepository.findByEmail(emailTratado).ifPresent(usuarioComMesmoEmail -> {
            if (!usuarioComMesmoEmail.getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new RuntimeException("Este e-mail já está cadastrado.");
            }
        });

        usuario.setNomeCompleto(nomeCompleto.trim());
        usuario.setEmail(emailTratado);
        usuario.setTelefone(telefoneLimpo);
        usuario.setSenha(passwordEncoder.encode(senha));

        if (usuario.getTemEmpresa() == null) {
            usuario.setTemEmpresa(0);
        }

        if (usuario.getAmbienteUsuario() == null) {
            usuario.setAmbienteUsuario(AmbienteUsuario.PESSOAL);
        }

        Usuario salvo = usuarioRepository.save(usuario);
        return mapearParaResponse(salvo);
    }

    public Optional<Usuario> buscarPorTelefone(String telefone) {
        return usuarioRepository.findByTelefone(limparTelefone(telefone));
    }

    private String limparTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }

        return telefone.replaceAll("\\D", "");
    }

    private UsuarioResponse mapearParaResponse(Usuario entity) {
        return new UsuarioResponse(
                entity.getIdUsuario(),
                entity.getNomeCompleto(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getAssinaturaAtiva()
        );
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void atualizarPerfil(Long idUsuario, PerfilDTO perfilDTO) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNomeCompleto(perfilDTO.nomeCompleto());
        usuario.setEmail(perfilDTO.email());
        usuario.setTelefone(limparTelefone(perfilDTO.telefone()));

        usuarioRepository.save(usuario);
    }

    public PerfilDTO buscarPerfil(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return new PerfilDTO(
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getAmbienteUsuario() != null
                        ? usuario.getAmbienteUsuario()
                        : AmbienteUsuario.PESSOAL,
                usuario.getTemEmpresa()
        );
    }

    public AmbientesDTO listarAmbientes(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean podeEmpresa = usuario.getTemEmpresa() != null
                && usuario.getTemEmpresa() == 1;

        return new AmbientesDTO(
                true,
                podeEmpresa
        );
    }

    @Transactional
    public void alterarAmbiente(Long idUsuario, AlterarAmbienteDTO alterarAmbienteDTO) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String ambienteSolicitado = alterarAmbienteDTO.ambiente();

        if ("EMPRESA".equalsIgnoreCase(ambienteSolicitado)) {
            boolean podeEmpresa = usuario.getTemEmpresa() != null
                    && usuario.getTemEmpresa() == 1;

            if (!podeEmpresa) {
                throw new RuntimeException("Você ainda não possui perfil empresarial cadastrado.");
            }

            usuario.setAmbienteUsuario(AmbienteUsuario.EMPRESA);
        } else {
            usuario.setAmbienteUsuario(AmbienteUsuario.PESSOAL);
        }

        usuarioRepository.save(usuario);
    }
}