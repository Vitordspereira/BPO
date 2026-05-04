package hubhds.bpo.dto.checkout;

import java.math.BigDecimal;

public record CheckoutAssinaturaResponse(
        String preCadastroToken,
        String proximaEtapa,
        String mpPreapprovalId,
        String mpStatus,
        String plano,
        BigDecimal valorPlano,
        String periodicidadePlano
) {
}