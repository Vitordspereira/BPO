package hubhds.bpo.controller.usuario;

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

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse resp = usuarioService.usuarioResponse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // Rota que a Emilly e o n8n vão usar
    @GetMapping("/status/{telefone}")
    public ResponseEntity<?> buscarStatusParaAutomacao(@PathVariable String telefone) {
        return usuarioService.buscarPorTelefone(telefone)
                .map(user -> {
                    // Cenário: Usuário ENCONTRADO no banco
                    // Retornamos os dados reais dele
                    return ResponseEntity.ok(Map.of(
                            "nome_completo", user.getNomeCompleto(),
                            "telefone", user.getTelefone(),
                            "status_ativo", user.getAssinaturaAtiva()
                    ));
                })
                .orElseGet(() -> {
                    return ResponseEntity.ok(Map.of(
                            "nome_completo", "Desconhecido",
                            "telefone", telefone,
                            "status_ativo", false,
                            "mensagem", "número de telefone não consta no banco de dados"
                    ));
                });
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

    @PatchMapping
    public ResponseEntity<?> concluirOnboarding(@PathVariable String email) {
        return usuarioService.buscarPorTelefone(email)
                .map(user -> {

                    usuarioService.salvar(user); // Você vai precisar criar esse metodo simples no Service
                    return ResponseEntity.ok("Onboarding finalizado com sucesso!");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
