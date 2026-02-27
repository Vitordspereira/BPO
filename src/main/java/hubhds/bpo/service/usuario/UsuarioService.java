package hubhds.bpo.service.usuario;

import hubhds.bpo.dto.usuario.UsuarioRequest;
import hubhds.bpo.dto.usuario.UsuarioResponse;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse usuarioResponse(UsuarioRequest usuarioRequest) {
        // 1. Verifica se o e-mail já existe no banco (Pode ter sido criado pela Hotmart)
        Optional<Usuario> usuarioExistente = usuarioRepository.findByTelefone(usuarioRequest.email());

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();

            // Se a senha for nula, significa que ele comprou na Hotmart e está criando a senha AGORA
            if (usuario.getSenha() == null) {
                usuario.setSenha(passwordEncoder.encode(usuarioRequest.senha()));

                // Atualiza outros campos que podem ter vindo vazios da Hotmart
                usuario.setTelefone(usuarioRequest.telefone());
                usuario.setTemEmpresa(usuarioRequest.temEmpresa());
                usuario.setCnpj(usuarioRequest.cnpj());

                Usuario salvo = usuarioRepository.save(usuario);
                return mapearParaResponse(salvo);
            } else {
                // Se já tem senha, o e-mail já está em uso por alguém que já completou o cadastro
                throw new RuntimeException("Este e-mail já possui uma conta cadastrada.");
            }
        }

        // 2. Se o usuário NÃO existe (Fluxo normal: se cadastrou no site antes de comprar)
        boolean temEmpresa = usuarioRequest.temEmpresa() == 1;

        Usuario novoUsuario = Usuario.builder()
                .nomeCompleto(usuarioRequest.nomeCompleto())
                .email(usuarioRequest.email())
                .telefone(usuarioRequest.telefone())
                .senha(passwordEncoder.encode(usuarioRequest.senha()))
                .cpf(usuarioRequest.cpf())
                .temEmpresa(temEmpresa ? 1 : 0)
                .cnpj(usuarioRequest.cnpj())
                .assinaturaAtiva(false) // No site ele começa sem acesso até o Webhook chegar
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);

        return mapearParaResponse(salvo);
    }

    // NOVO METODO: Adicione isso para a Controller conseguir consultar o banco
    public Optional<Usuario> buscarPorTelefone(String telefone) {
        return usuarioRepository.findByTelefone(telefone);
    }

    // Metodo auxiliar para transformar a Entity em DTO (Response)
    private UsuarioResponse mapearParaResponse(Usuario entity) {
        return new UsuarioResponse(
                entity.getIdUsuario(),
                entity.getNomeCompleto(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getCpf(),
                entity.getTemEmpresa(),
                entity.getCnpj(),
                entity.getAssinaturaAtiva() // Adicionei esse campo no seu Response!
        );
    }

    // 2. NOVO METODO PARA A HOTMART (Lógica do Fluxograma)
    @Transactional
    public void processarWebhookHotmart(String email, String status, String transaction, String nome, String cpf, String telefone) {

        boolean isAprovado = "COMPLETED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status);

        usuarioRepository.findByTelefone(email).ifPresentOrElse(
                existente -> {
                    // CENÁRIO: O CARA JÁ TINHA CADASTRO
                    // Apenas atualizamos o status da assinatura e a transação
                    existente.setAssinaturaAtiva(isAprovado);
                    existente.setHottTransaction(transaction);
                    usuarioRepository.save(existente);
                    System.out.println("Usuário já existia. Apenas ativamos o acesso!");
                },
                () -> {
                    // CENÁRIO: É UM CLIENTE NOVO
                    if (isAprovado) {
                        Usuario novo = Usuario.builder()
                                .nomeCompleto(nome)
                                .email(email)
                                .telefone(telefone)
                                .cpf(cpf)
                                .assinaturaAtiva(true)
                                .hottTransaction(transaction)
                                .temEmpresa(0)
                                .senha(null) // SENHA VAZIA: Ele vai criar depois no site
                                .build();
                        usuarioRepository.save(novo);
                        System.out.println("Novo usuário criado via Hotmart (sem senha).");
                    }
                }
        );
    }

    public void salvar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    //lista todos os usuários para emilly (pode ser que eu retire um dia isso aqui)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void atualizarStatusAssinatura(String telefone, String statusHotmart) {
        Usuario usuario = usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean isAprovado = "COMPLETED".equalsIgnoreCase(statusHotmart) || "APPROVED".equalsIgnoreCase(statusHotmart);

        if (isAprovado) {
            usuario.setAssinaturaAtiva(true);
            usuario.setDataInatividade(null);//limpa a adata quando está em dia
        } else {
            usuario.setAssinaturaAtiva(false);

            if (usuario.getDataInatividade() == null) {
                usuario.setDataInatividade(LocalDateTime.now());
            }
        }

        usuarioRepository.save(usuario);
    }
}
