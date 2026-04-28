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
        Optional<Usuario> usuarioExistente = usuarioRepository.findByTelefone(usuarioRequest.telefone());

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();

            // Se já existe usuário por telefone e ainda não tem senha,
            // significa que ele veio de fluxo externo e está finalizando cadastro agora
            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                usuario.setNomeCompleto(usuarioRequest.nomeCompleto());
                usuario.setEmail(usuarioRequest.email());
                usuario.setTelefone(usuarioRequest.telefone());
                usuario.setSenha(passwordEncoder.encode(usuarioRequest.senha()));

                if (usuario.getTemEmpresa() == null) {
                    usuario.setTemEmpresa(0);
                }

                if (usuario.getAmbienteUsuario() == null) {
                    usuario.setAmbienteUsuario(AmbienteUsuario.PESSOAL);
                }

                Usuario salvo = usuarioRepository.save(usuario);
                return mapearParaResponse(salvo);
            } else {
                throw new RuntimeException("Este telefone já possui uma conta cadastrada.");
            }
        }

        Usuario novoUsuario = Usuario.builder()
                .nomeCompleto(usuarioRequest.nomeCompleto())
                .email(usuarioRequest.email())
                .telefone(usuarioRequest.telefone())
                .senha(passwordEncoder.encode(usuarioRequest.senha()))
                .temEmpresa(0)
                .assinaturaAtiva(false)
                .ambienteUsuario(AmbienteUsuario.PESSOAL)
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);
        return mapearParaResponse(salvo);
    }

    @Transactional
    public UsuarioResponse completarCadastroPosPagamento(UsuarioCompletaCadastro usuarioCompletaCadastro) {
        Usuario usuario = usuarioRepository.findByTelefone(usuarioCompletaCadastro.telefone())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para esse telefone."));

        boolean assinaturaValida = Boolean.TRUE.equals(usuario.getAssinaturaAtiva())
                || "authorized".equalsIgnoreCase(usuario.getMpStatus());

        if (!assinaturaValida) {
            throw new RuntimeException("Cadastro não permitido: assinatura ainda não está ativa.");
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            throw new RuntimeException("Este usuário já concluiu o cadastro.");
        }

        usuario.setNomeCompleto(usuarioCompletaCadastro.nomeCompleto().trim());
        usuario.setEmail(usuarioCompletaCadastro.email().trim().toLowerCase());
        usuario.setTelefone(usuarioCompletaCadastro.telefone().trim());
        usuario.setSenha(passwordEncoder.encode(usuarioCompletaCadastro.senha()));

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
        return usuarioRepository.findByTelefone(telefone);
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
        usuario.setTelefone(perfilDTO.telefone());

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