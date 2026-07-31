package hubhds.bpo.dto.n8n.editar;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record N8nAtualizarRequest(
        BigDecimal valor,
        String descricao,
        String categoria,
        String movimentacao,
        @JsonProperty("tipo_gasto")
        @JsonAlias({"tipoGasto", "tipo_de_gasto", "perfil_financeiro", "perfilFinanceiro"})
        String tipoGasto,
        @JsonProperty("forma_pagamento")
        String formaPagamento,
        String status
) {
}
