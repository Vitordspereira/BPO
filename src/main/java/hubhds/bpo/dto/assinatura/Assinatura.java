package hubhds.bpo.dto.assinatura;


import java.math.BigDecimal;

public record Assinatura(
        String preapprovalId,
        String initPoint,
        String sandboxInitPoint,
        String status,
        String tipoPlano,
        BigDecimal valor,
        String periodicidade
) {
}