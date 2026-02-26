package hubhds.bpo.dto.dashboard;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.MeioPagamento;
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

        @NotNull
        MeioPagamento meioPagamento,

        @NotNull
        Long idCategoria,

        Long idCartao
) {
}
