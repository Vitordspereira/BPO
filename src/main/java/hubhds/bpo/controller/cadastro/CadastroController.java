package hubhds.bpo.controller.cadastro;

import hubhds.bpo.dto.cadastro.CadastroRequest;
import hubhds.bpo.dto.cadastro.CadastroResponse;
import hubhds.bpo.model.cadastro.Cadastro;
import hubhds.bpo.service.cadastro.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cadastro")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @PostMapping
    public ResponseEntity<CadastroResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        CadastroResponse resp = cadastroService.cadastroResponse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // Rota que a Emilly e o n8n vão usar
    @GetMapping("/status/{email}")
    public ResponseEntity<?> buscarStatusParaAutomacao(@PathVariable String email) {
        return cadastroService.buscarPorEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "email", user.getEmail(),
                        "status_ativo", user.getAssinaturaAtiva(),
                        "primeiro_acesso", user.getPrimeiroAcesso(),
                        "onboarding", user.getOnboardingConcluido(),
                        "telefone", user.getTelefone()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> listarTodosAutomacao() {
        List<Cadastro> todos = cadastroService.listarTodos();

        List<Map<String, Object>> resposta = todos.stream().map(user -> Map.<String, Object>of(
                "email", user.getEmail() != null ? user.getEmail() : "N/A",
                "status_ativo", user.getAssinaturaAtiva(),
                "primeiro_acesso", user.getPrimeiroAcesso(),
                "onboarding", user.getOnboardingConcluido(),
                "telefone", user.getTelefone() != null ? user.getTelefone() : ""
        )).toList();

        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/concluir-onboarding/{email}")
    public ResponseEntity<?> concluirOnboarding(@PathVariable String email) {
        return cadastroService.buscarPorEmail(email)
                .map(user -> {
                    user.setPrimeiroAcesso(false);
                    user.setOnboardingConcluido(true);
                    cadastroService.salvar(user); // Você vai precisar criar esse metodo simples no Service
                    return ResponseEntity.ok("Onboarding finalizado com sucesso!");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
