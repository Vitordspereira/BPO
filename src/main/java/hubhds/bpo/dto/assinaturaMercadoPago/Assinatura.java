package hubhds.bpo.dto.assinaturaMercadoPago;

import java.math.BigDecimal;

public record Assinatura(
        String preApprovalId,
        String initPoint,
        String sandboxInitPoint,
        String status,
        String tipoPlano,
        BigDecimal valor,
        String periodicidade
) {
}
