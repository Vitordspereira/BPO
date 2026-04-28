package hubhds.bpo.model.n8n;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacoes_n8n")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class N8n {

    @Id
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "draft_id", nullable = false)
    private String draftId;

    @Column(name = "telefone", length = 50, nullable = false)
    private String telefone;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "data_transacao", nullable = false)
    private LocalDate dataTransacao;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "movimentacao", length = 100)
    private String movimentacao;

    @Column(name = "tipo_gasto", length = 100)
    private String tipoGasto;

    @Column(name = "forma_pagamento", length = 100)
    private String formaPagamento;

    /*
     @Enumerated(EnumType.STRING)
     @Column(name = "perfil_financeiro", length = 50, nullable = false)
     private PerfilFinanceiro perfilFinanceiro;
    */

    @Column(name = "status", length = 100, nullable = false)
    private String status;
}
