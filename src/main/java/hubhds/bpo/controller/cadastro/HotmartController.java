package hubhds.bpo.controller.cadastro;

import hubhds.bpo.service.cadastro.CadastroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")// URL que você vai colocar na Hotmart
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HotmartController {

    private final CadastroService cadastroService;

    public HotmartController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @PostMapping("/hotmart")
    public ResponseEntity<Void> receberWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // "Garimpando" os dados do JSON que você me mandou
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> buyer = (Map<String, Object>) data.get("buyer");
            Map<String, Object> purchase = (Map<String, Object>) data.get("purchase");

            String email = (String) buyer.get("email");
            String nome = (String) buyer.get("name");
            String cpf = (String) buyer.get("document"); // Pegando o CPF real
            String telefone = (String) buyer.get("checkout_phone");
            String status = (String) purchase.get("status");
            String transaction = (String) purchase.get("transaction");

            // Envia para o Service processar a lógica do seu fluxograma
            cadastroService.processarWebhookHotmart(email, status, transaction, nome, cpf, telefone);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Se algo der errado, logamos o erro e avisamos a Hotmart para tentar de novo
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
