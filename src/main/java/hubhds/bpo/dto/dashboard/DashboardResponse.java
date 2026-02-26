package hubhds.bpo.dto.dashboard;

import hubhds.bpo.model.dashboard.Dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
        Long idDashboard,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        String tipo,
        String meioPagamento,
        String nomeCategoria,
        String corCategoria, // Importante para o gráfico da Emilly!
        String iconeCategoria,
        String nomeCartao
) {
    // Construtor compacto para converter a Entity Fluxo diretamente para o Response
    public DashboardResponse(Dashboard dashboard) {
        this(
                dashboard.getIdDashboard(),
                dashboard.getDescricao(),
                dashboard.getValor(),
                dashboard.getData(),
                dashboard.getTipo().name(),
                dashboard.getMeioPagamento().name(),
                dashboard.getCategoria().getNome(),
                dashboard.getCategoria().getCor(),
                dashboard.getCategoria().getIcone(),
                dashboard.getCartao() != null ? dashboard.getCartao().getNomeCartao() : null
        ); {
        }
    }
}