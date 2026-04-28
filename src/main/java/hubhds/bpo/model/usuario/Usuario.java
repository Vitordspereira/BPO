package hubhds.bpo.model.usuario;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_usuario_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_usuario_cnpj", columnNames = "cnpj")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 20)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^55\\d{10,11}$", message = "Informe o telefone completo")
    @Size(min = 12, max = 20)
    private String telefone;

    @Column(name = "senha", length = 100)
    private String senha;

    @Column(name = "tem_empresa", nullable = false)
    private Integer temEmpresa;

    //Campo em que o sistema usa para liberar ou bloquear acesso do usuário
    @Column(name = "assinatura_ativa")
    private Boolean assinaturaAtiva = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambiente_usuario", length = 20)
    private AmbienteUsuario ambienteUsuario;

    //Registra quando o usuário perdeu o acesso
    @Column(name = "data_inatividade")
    private LocalDateTime dataInatividade;

    //não remover ainda
    @Column(name = "hott_transaction", length = 50)
    private String hottTransaction;

    //ID de assinatura criada no mercado pago
    @Column(name = "mp_preapproval_id", length = 100)
    private String mpPreapprovalId;

    //referência para conciliar webhook e usuário
    @Column(name = "mp_external_reference", length = 100)
    private String mpExternalReference;

    //Status atual da assinatura do mercado pago
    @Column(name = "mp_status", length = 30)
    private String mpStatus;

    //Quando a assinatura foi atualizada pela última vez no sistema
    @Column(name = "mp_assinatura_atualizada_em")
    private LocalDateTime mpAssinaturaAtualizadaEm;

    //tipo de plano contratado: MENSAL OU ANUAL
    @Column(name = "tipo_plano", length = 20)
    private String tipoPlano;

    //valor do plano contratado
    @Column(name = "valor_plano", precision = 10, scale = 2)
    private BigDecimal valorPlano;

    //De quanto em quanto tempo o plano é cobrado
    @Column(name = "periodicidade_plano", length = 20)
    private String periodicidadePlano;

    @CreationTimestamp
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    public void prePersist(){
        if (assinaturaAtiva == null) {
            assinaturaAtiva = false;
        }

        if (temEmpresa == null) {
            temEmpresa = 0;
        }
    }
}
