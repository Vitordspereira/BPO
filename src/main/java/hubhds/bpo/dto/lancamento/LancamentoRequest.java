package hubhds.bpo.dto.lancamento;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoRequest(

        @NotNull(message = "idCategoria é obrigatório")
        Long idCategoria,

        @NotNull(message = "movimentacao é obrigatória")
        Tipo movimentacao,

        @NotNull(message = "valor é obrigatório")
        @Positive(message = "valor deve ser maior que zero")
        BigDecimal valor,

        String descricao,

        @NotNull(message = "dataTransacao é obrigatória")
        LocalDate dataTransacao,

        @NotNull(message = "formaPagamento é obrigatória")
        MeioPagamento formaPagamento,

        @NotNull(message = "tipoGasto é obrigatório")
        PerfilFinanceiro tipoGasto,

        Long idDashboard,

        String transactionId

) {
}