package hubhds.bpo.controller.categorian8n;

import hubhds.bpo.dto.categorian8n.CategoriaN8nRequest;
import hubhds.bpo.dto.categorian8n.CategoriaN8nResponse;
import hubhds.bpo.model.categorian8n.CategoriaN8n;
import hubhds.bpo.repository.categorian8n.CategoriaN8nRepository;
import hubhds.bpo.repository.n8n.TransacaoN8nRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("categoria/n8n")
public class CategoriaN8nController {

    private final CategoriaN8nRepository categoriaN8nRepository;
    private final TransacaoN8nRepository transacaoN8nRepository;

    public CategoriaN8nController(CategoriaN8nRepository categoriaN8nRepository, TransacaoN8nRepository transacaoN8nRepository) {
        this.categoriaN8nRepository = categoriaN8nRepository;
        this.transacaoN8nRepository = transacaoN8nRepository;
    }

    @GetMapping("/{telefone}")
    public ResponseEntity<List<CategoriaN8nResponse>> listarPorTelefone(
            @PathVariable String telefone
    ) {
        List<CategoriaN8nResponse> categoriaN8nResponses = categoriaN8nRepository
                .findByTelefoneOrderByNomeAsc(telefone)
                .stream()
                .map(CategoriaN8nResponse::new)
                .toList();

        return ResponseEntity.ok(categoriaN8nResponses);
    }

    @PutMapping("/{telefone}/{idCategoriaN8n}")
    public ResponseEntity<?> editarCategoria(
            @PathVariable String telefone,
            @PathVariable Long idCategoriaN8n,
            @RequestBody CategoriaN8nRequest categoriaN8nRequest
    ) {
        CategoriaN8n categoriaN8n = categoriaN8nRepository
                .findByIdCategoriaN8nAndTelefone(idCategoriaN8n, telefone)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (categoriaN8nRequest.nome() != null && !categoriaN8nRequest.nome().isBlank()) {
            categoriaN8n.setNome(categoriaN8nRequest.nome().trim());
        }

        if (categoriaN8nRequest.tipo() != null && !categoriaN8nRequest.tipo().isBlank()) {
            categoriaN8n.setTipo(categoriaN8nRequest.tipo().trim().toUpperCase());
        }

        if (categoriaN8nRequest.icone() != null && !categoriaN8nRequest.icone().isBlank()) {
            categoriaN8n.setIcone(categoriaN8nRequest.icone().trim());
        }

        if (categoriaN8nRequest.cor() != null && !categoriaN8nRequest.cor().isBlank()) {
            categoriaN8n.setCor(categoriaN8nRequest.cor().trim());
        }

        if (categoriaN8nRequest.perfilFinanceiro() != null && !categoriaN8nRequest.perfilFinanceiro().isBlank()) {
            categoriaN8n.setPerfilFinanceiro(categoriaN8nRequest.perfilFinanceiro().trim().toUpperCase());
        }

        CategoriaN8n salva = categoriaN8nRepository.save(categoriaN8n);

        return ResponseEntity.ok(new CategoriaN8nResponse(salva));
    }

    @Transactional
    @DeleteMapping("/{telefone}/{idCategoriaN8n}")
    public ResponseEntity<?> excluirCategoria(
            @PathVariable String telefone,
            @PathVariable Long idCategoriaN8n
    ) {
        String telefoneTratado = telefone != null ? telefone.trim() : null;

        if (telefoneTratado == null || telefoneTratado.isBlank()) {
            return ResponseEntity.badRequest().body("telefone é obrigatório.");
        }

        CategoriaN8n categoriaN8n = categoriaN8nRepository
                .findByIdCategoriaN8nAndTelefone(idCategoriaN8n, telefoneTratado)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        String nomeCategoria = categoriaN8n.getNome();
        String perfilFinanceiro = categoriaN8n.getPerfilFinanceiro();

        if (perfilFinanceiro == null || perfilFinanceiro.isBlank()) {
            return ResponseEntity.badRequest().body("Perfil Financeiro da categoria não encontrado.");
        }

        transacaoN8nRepository
                .deleteTransacoesPorCategoriaPerfil(
                        telefoneTratado,
                        nomeCategoria,
                        perfilFinanceiro
                );

        categoriaN8nRepository.delete(categoriaN8n);

        return ResponseEntity.ok("Categoria excluída com sucesso.");
    }
}