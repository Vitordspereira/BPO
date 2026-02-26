package hubhds.bpo.dto.dashboard.resumo;

import hubhds.bpo.model.dashboard.Dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoDTO(
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal SaldoFinal,
        List<Dashboard> lancamento
) {
}
