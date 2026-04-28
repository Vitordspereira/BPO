package hubhds.bpo.dto.dashboard;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardRequest(

        @NotBlank
        String descricao,

        @NotNull
        BigDecimal valor,

        @NotNull
        LocalDate data,

        @NotNull
        Tipo tipo,

        MeioPagamento meioPagamento,

        @NotBlank
        String categoria,
        Long idUsuario,
        Long idCartao,
        PerfilFinanceiro perfilFinanceiro
) {
}
