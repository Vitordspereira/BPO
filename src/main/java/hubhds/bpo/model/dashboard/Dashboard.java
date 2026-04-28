package hubhds.bpo.model.dashboard;

import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.categoria.Tipo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "dashboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dashboard")
    private Long idDashboard;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    private MeioPagamento meioPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_financeiro", nullable = false)
    private PerfilFinanceiro perfilFinanceiro;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_cartao")
    private Cartao cartao;
}
