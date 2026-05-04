package hubhds.bpo.controller.checkout;

import hubhds.bpo.dto.checkout.CheckoutAssinaturaRequest;
import hubhds.bpo.dto.usuario.UsuarioCompletaCadastro;
import hubhds.bpo.service.checkout.CheckoutAssinaturaService;
import hubhds.bpo.service.preCadastro.PreCadastroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/checkout")
@CrossOrigin(origins = "*")
public class CheckoutAssinaturaController {

    private final CheckoutAssinaturaService checkoutAssinaturaService;
    private final PreCadastroService preCadastroService;

    public CheckoutAssinaturaController(
            CheckoutAssinaturaService checkoutAssinaturaService,
            PreCadastroService preCadastroService
    ) {
        this.checkoutAssinaturaService = checkoutAssinaturaService;
        this.preCadastroService = preCadastroService;
    }

    @PostMapping("/assinatura")
    public ResponseEntity<?> assinar(@RequestBody CheckoutAssinaturaRequest request) {
        try {
            return ResponseEntity.ok(checkoutAssinaturaService.assinar(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    @PostMapping("/pre-cadastro")
    public ResponseEntity<?> finalizarPreCadastro(@Valid @RequestBody UsuarioCompletaCadastro request) {
        try {
            return ResponseEntity.ok(preCadastroService.finalizar(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }
}