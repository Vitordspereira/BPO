package hubhds.bpo.service.cadastro;

import hubhds.bpo.dto.cadastro.CadastroRequest;
import hubhds.bpo.dto.cadastro.CadastroResponse;
import hubhds.bpo.model.cadastro.Cadastro;
import hubhds.bpo.repository.cadastro.CadastroRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CadastroService {

    private final CadastroRepository cadastroRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(CadastroRepository cadastroRepository, PasswordEncoder passwordEncoder) {
        this.cadastroRepository = cadastroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastroResponse(CadastroRequest cadastroRequest) {
        // 1. Verifica se o e-mail já existe no banco (Pode ter sido criado pela Hotmart)
        Optional<Cadastro> usuarioExistente = cadastroRepository.findByEmail(cadastroRequest.email());

        if (usuarioExistente.isPresent()) {
            Cadastro usuario = usuarioExistente.get();

            // Se a senha for nula, significa que ele comprou na Hotmart e está criando a senha AGORA
            if (usuario.getSenha() == null) {
                usuario.setSenha(passwordEncoder.encode(cadastroRequest.senha()));

                // Atualiza outros campos que podem ter vindo vazios da Hotmart
                usuario.setTelefone(cadastroRequest.telefone());
                usuario.setTemEmpresa(cadastroRequest.temEmpresa());
                usuario.setCnpj(cadastroRequest.cnpj());

                Cadastro salvo = cadastroRepository.save(usuario);
                return mapearParaResponse(salvo);
            } else {
                // Se já tem senha, o e-mail já está em uso por alguém que já completou o cadastro
                throw new RuntimeException("Este e-mail já possui uma conta cadastrada.");
            }
        }

        // 2. Se o usuário NÃO existe (Fluxo normal: se cadastrou no site antes de comprar)
        boolean temEmpresa = cadastroRequest.temEmpresa() == 1;

        Cadastro novoUsuario = Cadastro.builder()
                .nomeCompleto(cadastroRequest.nomeCompleto())
                .email(cadastroRequest.email())
                .telefone(cadastroRequest.telefone())
                .senha(passwordEncoder.encode(cadastroRequest.senha()))
                .cpf(cadastroRequest.cpf())
                .temEmpresa(temEmpresa ? 1 : 0)
                .cnpj(cadastroRequest.cnpj())
                .assinaturaAtiva(false) // No site ele começa sem acesso até o Webhook chegar
                .build();

        Cadastro salvo = cadastroRepository.save(novoUsuario);

        return mapearParaResponse(salvo);
    }

    // NOVO METODO: Adicione isso para a Controller conseguir consultar o banco
    public Optional<Cadastro> buscarPorEmail(String email) {
        return cadastroRepository.findByEmail(email);
    }

    // Metodo auxiliar para transformar a Entity em DTO (Response)
    private CadastroResponse mapearParaResponse(Cadastro entity) {
        return new CadastroResponse(
                entity.getIdCadastro(),
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

        cadastroRepository.findByEmail(email).ifPresentOrElse(
                existente -> {
                    // CENÁRIO: O CARA JÁ TINHA CADASTRO
                    // Apenas atualizamos o status da assinatura e a transação
                    existente.setAssinaturaAtiva(isAprovado);
                    existente.setHottTransaction(transaction);
                    cadastroRepository.save(existente);
                    System.out.println("Usuário já existia. Apenas ativamos o acesso!");
                },
                () -> {
                    // CENÁRIO: É UM CLIENTE NOVO
                    if (isAprovado) {
                        Cadastro novo = Cadastro.builder()
                                .nomeCompleto(nome)
                                .email(email)
                                .telefone(telefone)
                                .cpf(cpf)
                                .assinaturaAtiva(true)
                                .hottTransaction(transaction)
                                .temEmpresa(0)
                                .senha(null) // SENHA VAZIA: Ele vai criar depois no site
                                .build();
                        cadastroRepository.save(novo);
                        System.out.println("Novo usuário criado via Hotmart (sem senha).");
                    }
                }
        );
    }

    public void salvar(Cadastro cadastro) {
        cadastroRepository.save(cadastro);
    }

    //lista todos os usuários para emilly
    public List<Cadastro> listarTodos() {
        return cadastroRepository.findAll();
    }
}
