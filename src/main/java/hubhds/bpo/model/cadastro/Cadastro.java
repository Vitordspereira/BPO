package hubhds.bpo.model.cadastro;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cadastro",
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
public class Cadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cadastro", nullable = false)
    private Long idCadastro;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "senha", length = 100)
    private String senha;

    @Column(name = "cpf", nullable = false, length = 14)
    private String cpf;

    @Column(name = "tem_empresa", nullable = false)
    private Integer temEmpresa;

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "assinatura_ativa")
    private Boolean assinaturaAtiva;

    @Column(name = "hott_transaction", length = 50)
    private String hottTransaction;

    // NOVOS CAMPOS PARA O FLUXO DA EMILLY
    @Column(name = "primeiro_acesso", nullable = false)
    @Builder.Default
    private Boolean primeiroAcesso = true;

    @Column(name = "onboarding_concluido", nullable = false)
    @Builder.Default
    private Boolean onboardingConcluido = false;

    @CreationTimestamp
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;
}

