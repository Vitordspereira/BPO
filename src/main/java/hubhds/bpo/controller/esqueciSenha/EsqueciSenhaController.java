package hubhds.bpo.controller.esqueciSenha;

import hubhds.bpo.dto.esqueciSenha.EsqueciSenhaRequest;
import hubhds.bpo.dto.esqueciSenha.RedefinirSenhaResponse;
import hubhds.bpo.service.esqueciSenha.EsqueciSenhaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conta")
@CrossOrigin(origins = "*")
public class EsqueciSenhaController {

    private final EsqueciSenhaService esqueciSenhaService;

    public EsqueciSenhaController(EsqueciSenhaService esqueciSenhaService) {
        this.esqueciSenhaService = esqueciSenhaService;
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody EsqueciSenhaRequest esqueciSenhaRequest) {
        String link = esqueciSenhaService.solicitarRecuperacao(esqueciSenhaRequest.email());

        return ResponseEntity.ok(link);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaResponse redefinirSenhaResponse) {
        esqueciSenhaService.redefinirSenha(redefinirSenhaResponse.token(), redefinirSenhaResponse.novaSenha());

        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}
