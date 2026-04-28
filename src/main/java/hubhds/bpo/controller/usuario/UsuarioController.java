package hubhds.bpo.controller.usuario;

import hubhds.bpo.dto.usuario.UsuarioCompletaCadastro;
import hubhds.bpo.dto.usuario.UsuarioRequest;
import hubhds.bpo.dto.usuario.UsuarioResponse;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse resp = usuarioService.cadastroManual(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/completar-cadastro")
    public ResponseEntity<?> completarCadastro(@Valid @RequestBody UsuarioCompletaCadastro usuarioCompletaCadastro) {
        try {
            UsuarioResponse usuarioResponse = usuarioService.completarCadastroPosPagamento(usuarioCompletaCadastro);
            return ResponseEntity.ok(usuarioResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // Rota que a Emilly e o n8n vão usar
    @GetMapping("/status/{telefone}")
    public ResponseEntity<?> buscarStatusParaAutomacao(@PathVariable String telefone) {
        return usuarioService.buscarPorTelefone(telefone)
                .map(user -> {
                    String status = user.getMpStatus() == null ? "" : user.getMpStatus().toLowerCase();

                    boolean cadastroConcluido = user.getSenha() != null && !user.getSenha().isBlank();

                    String tipoUsuario = cadastroConcluido ? "cadastrado" : "cadastro pendente";

                    String nomeExibicao = cadastroConcluido
                            ? user.getNomeCompleto()
                            : "Cadastro pendente";

                    String etapa = cadastroConcluido
                            ? descreverEtapa(status)
                            : "assinatura em andamento";


                    return ResponseEntity.ok(Map.of(
                            "nome_completo", nomeExibicao,
                            "telefone", user.getTelefone(),
                            "status", status,
                            "status_ativo", Boolean.TRUE.equals(user.getAssinaturaAtiva()),
                            "etapa", etapa,
                            "tipo_usuario", tipoUsuario
                    ));
                })
                .orElseGet(() -> {
                    return ResponseEntity.ok(Map.of(
                            "nome_completo", "Desconhecido",
                            "telefone", telefone,
                            "status", "",
                            "status_ativo", false,
                            "etapa", "usuário não encontrado",
                            "mensagem", "número de telefone não consta no banco de dados"
                    ));
                });
    }

    private String descreverEtapa(String status) {
        if (status == null || status.isBlank()) {
            return "sem assinatura de mercado pago";
        }

        return switch (status.toLowerCase()) {
            case "authorized" -> "assinatura ativa";
            case "pending" -> "aguardando pagamento";
            case "paused" -> "assinatura pausada";
            case "cancelled" -> "assinatura cancelada";
            default -> "status desconhecido";
        };
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> listarTodosAutomacao() {
        List<Usuario> todos = usuarioService.listarTodos();

        List<Map<String, Object>> resposta = todos.stream().map(user -> Map.<String, Object>of(
                "email", user.getEmail() != null ? user.getEmail() : "N/A",
                "status_ativo", user.getAssinaturaAtiva(),
                "telefone", user.getTelefone() != null ? user.getTelefone() : ""
        )).toList();

        return ResponseEntity.ok(resposta);
    }
}
