package hubhds.bpo.controller.assinaturaMercadoPago;

import hubhds.bpo.dto.assinaturaMercadoPago.Assinatura;
import hubhds.bpo.dto.assinaturaMercadoPago.CriarAssinatura;
import hubhds.bpo.dto.assinaturaMercadoPago.SincronizarAssinatura;
import hubhds.bpo.service.AssinaturaMercadoPagoService.AssinaturaMercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaMercadoPagoService assinaturaMercadoPagoService;


    public AssinaturaController(AssinaturaMercadoPagoService assinaturaMercadoPagoService) {
        this.assinaturaMercadoPagoService = assinaturaMercadoPagoService;
    }

    @PostMapping("/mercado-pago/{idUsuario}")
    public ResponseEntity<Assinatura> criar(@PathVariable Long idUsuario, @RequestParam(defaultValue = "MENSAL") String plano, @RequestParam(required = false) String payerEmail) {
        return ResponseEntity.ok(assinaturaMercadoPagoService.assinatura(idUsuario, plano, payerEmail));
    }

    //
    @PostMapping("mercado-pago/iniciar")
    public ResponseEntity<Assinatura> iniciar(@RequestBody CriarAssinatura criarAssinatura) {
        return ResponseEntity.ok(
                assinaturaMercadoPagoService.iniciarAssinatura(
                        criarAssinatura.telefone(),
                        criarAssinatura.plano(),
                        criarAssinatura.payerEmail()
                )
        );
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizar(@RequestBody SincronizarAssinatura sincronizarAssinatura) {
        return ResponseEntity.ok(assinaturaMercadoPagoService.sincronizarAssinatura(sincronizarAssinatura.preapprovalId()));
    }
}
