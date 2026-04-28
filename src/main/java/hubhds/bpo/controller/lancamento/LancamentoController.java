package hubhds.bpo.controller.lancamento;

import hubhds.bpo.dto.lancamento.LancamentoRequest;
import hubhds.bpo.dto.lancamento.LancamentoResponse;
import hubhds.bpo.dto.lancamento.editar.LancamentoAtualizar;
import hubhds.bpo.service.lancamento.LancamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lancamento")
@CrossOrigin(origins = "*")
public class LancamentoController {

    private final LancamentoService lancamentoService;

    public LancamentoController(LancamentoService lancamentoService) {
        this.lancamentoService = lancamentoService;
    }

    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> criarLancamento(
            @PathVariable Long idUsuario,
            @RequestBody @Valid LancamentoRequest lancamentoRequest
    ) {
        try {

            LancamentoResponse response = lancamentoService.criarLancamento(idUsuario, lancamentoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @PutMapping("/{idUsuario}/{idLancamento}")
    public ResponseEntity<?> atualizarLancamento(
            @PathVariable Long idUsuario,
            @PathVariable Long idLancamento,
            @RequestBody @Valid LancamentoAtualizar lancamentoAtualizar
    ){
        try {
            LancamentoResponse lancamentoResponse = lancamentoService.atualizarLancamento(idUsuario, idLancamento, lancamentoAtualizar);
            return ResponseEntity.ok(lancamentoResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    @DeleteMapping("/{idUsuario}/{idLancamento}")
    public ResponseEntity<?> excluirLancamento(
            @PathVariable Long idUsuario,
            @PathVariable Long idLancamento
    ){
        try {
            lancamentoService.excluirLancamento(idUsuario, idLancamento);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }
}
