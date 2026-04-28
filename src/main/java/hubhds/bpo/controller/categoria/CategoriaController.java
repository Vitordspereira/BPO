package hubhds.bpo.controller.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.dto.categoria.categoriaComLancamento.CategoriaComLancamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.service.categoria.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> criar(
            @PathVariable Long idUsuario,
            @RequestBody @Valid CategoriaRequest categoriaRequest
    ) {
        try {
            CategoriaResponse response = categoriaService.salvar(idUsuario, categoriaRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @PostMapping("/{idUsuario}/com-lancamento")
    public ResponseEntity<?> criarComLancamento(
            @PathVariable Long idUsuario,
            @RequestBody @Valid CategoriaComLancamento categoriaComLancamento
    ) {
        try {
            CategoriaResponse categoriaResponse = categoriaService.salvarLancamento(idUsuario, categoriaComLancamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoriaResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Long idUsuario,
            @RequestParam PerfilFinanceiro perfilFinanceiro
    ) {
        try {
            List<CategoriaResponse> categorias = categoriaService.listarPorUsuario(idUsuario, perfilFinanceiro);
            return ResponseEntity.ok(categorias);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @PutMapping("/editar/{idUsuario}/{categoriaRef}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long idUsuario,
            @PathVariable String categoriaRef,
            @RequestBody @Valid CategoriaRequest categoriaRequest
    ) {
        try {
            CategoriaResponse response = categoriaService.atualizarPorReferencia(idUsuario, categoriaRef, categoriaRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @DeleteMapping("/excluir/{idUsuario}/{idCategoria}")
    public ResponseEntity<?> excluir(
            @PathVariable Long idUsuario,
            @PathVariable Long idCategoria
    ) {
        try {
            categoriaService.deletar(idUsuario, idCategoria);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }
}