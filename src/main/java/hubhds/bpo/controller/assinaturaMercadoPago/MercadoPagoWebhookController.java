package hubhds.bpo.controller.assinaturaMercadoPago;


import hubhds.bpo.service.AssinaturaMercadoPagoService.MercadoPagoWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/webhooks")
public class MercadoPagoWebhookController {

    private final MercadoPagoWebhookService mercadoPagoWebhookService;

    public MercadoPagoWebhookController(MercadoPagoWebhookService mercadoPagoWebhookService) {
        this.mercadoPagoWebhookService = mercadoPagoWebhookService;
    }

    @PostMapping("/mercado-pago")
    public ResponseEntity<Void> receberWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestBody Map<String, Object> payload
    ) {
        System.out.println("Webhook Mercado Pago recebido");
        System.out.println("x-signature: " + xSignature);
        System.out.println("payload: " + payload);

        //primeiro deixa processa o fluxo
        mercadoPagoWebhookService.processar(payload);
        return ResponseEntity.ok().build();
    }

    //Retorno do mercado pago
    @GetMapping("/retorno")
    public ResponseEntity<String> retornaMercadoPago() {
        return ResponseEntity.ok("Mercado Pago recebido com sucesso");
    }
}
