package hubhds.bpo.dto.lancamento;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoResponse(
        Long idLancamento,
        String transactionId,
        Long idCategoria,
        Long idDashboard,
        String categoria,
        Tipo movimentacao,
        BigDecimal valor,
        String descricao,
        LocalDate dataTransacao,
        MeioPagamento formaPagamento,
        PerfilFinanceiro tipoGasto
) {
}
