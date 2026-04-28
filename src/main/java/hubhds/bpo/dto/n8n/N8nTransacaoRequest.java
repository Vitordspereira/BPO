package hubhds.bpo.dto.n8n;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import hubhds.bpo.model.usuario.PerfilFinanceiro;

public record N8nTransacaoRequest(
        @JsonProperty("draft_id")
        String draftId,

        String telefone,

        String origem,

        BigDecimal valor,

        String moeda,

        @JsonProperty("data_transacao")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataTransacao,

        String descricao,

        String categoria,

        String movimentacao,

        @JsonProperty("tipo_gasto")
        String tipoGasto,

        @JsonProperty("forma_pagamento")
        String formaPagamento,

        String perfilFinanceiro,

        String status
) {
}
