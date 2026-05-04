package hubhds.bpo.model.preCadastro;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_cadastro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreCadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pre_cadastro")
    private Long idPreCadastro;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "payer_email", nullable = false, length = 100)
    private String payerEmail;

    @Column(name = "plano", nullable = false, length = 50)
    private String plano;

    @Column(name = "valor_plano", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPlano;

    @Column(name = "periodicidade_plano", nullable = false, length = 50)
    private String periocidadePlano;

    @Column(name = "mp_preapproval_id", length = 100)
    private String mpPreapprovalId;

    @Column(name = "mp_external_reference", length = 100)
    private String mpExternalReference;

    @Column(name = "mp_status", length = 80)
    private String mpStatus;

    @Column(name = "usado", nullable = false)
    private Boolean usado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }

        if (this.usado == null) {
            this.usado = false;
        }

        this.atualizadoEm = agora;

        if (this.expiracao == null) {
            this.expiracao = agora.plusDays(2);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
