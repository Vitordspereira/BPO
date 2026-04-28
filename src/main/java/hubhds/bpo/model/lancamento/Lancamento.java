package hubhds.bpo.model.lancamento;

import com.fasterxml.jackson.annotation.JsonFormat;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lancamento")
    private Long idLancamento;

    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "id_dashboard")
    private Long idDashboard;

    @Enumerated(EnumType.STRING)
    @Column(name = "movimentacao", nullable = false)
    private Tipo movimentacao;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "descricao")
    private String descricao;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Column(name = "dataTransacao", nullable = false)
    private LocalDate dataTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private MeioPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gasto", nullable = false)
    private PerfilFinanceiro tipoGasto;
}
