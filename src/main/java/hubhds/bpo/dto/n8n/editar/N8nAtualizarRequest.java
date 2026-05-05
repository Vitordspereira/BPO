package hubhds.bpo.dto.n8n.editar;

import java.math.BigDecimal;

public record N8nAtualizarRequest(
        BigDecimal valor,
        String descricao,
        String categoria,
        String movimentacao,
        String tipoGasto,
        String formaPagamento,
        String status
) {
}
