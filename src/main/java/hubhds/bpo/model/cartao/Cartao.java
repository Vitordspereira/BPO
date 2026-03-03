package hubhds.bpo.model.cartao;

import hubhds.bpo.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartao;

    @Column(nullable = false, length = 100)
    private String nomeCartao;

    @Column(name = "numero_mascara", nullable = false, length = 19)
    private String numeroMascara;

    private Integer diaFechamento;
    private Integer diaVencimento;

    @Enumerated(EnumType.STRING)
    private Bandeira bandeira;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name ="status_cartao")
    private StatusCartao statusCartao = StatusCartao.ATIVO;

    @Column(precision = 10, scale = 2)
    private BigDecimal limiteTotal;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private BigDecimal saldoEntrada = BigDecimal.ZERO;
    private BigDecimal saldoSaida = BigDecimal.ZERO;

    private LocalDateTime dataCriacao = LocalDateTime.now();

}
