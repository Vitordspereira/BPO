package hubhds.bpo.model.categorian8n;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "categoria_n8n")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaN8n {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_n8n")
    private Long idCategoriaN8n;

    @Column(name = "telefone", nullable = false, length = 50)
    private String telefone;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "icone", length = 50)
    private String icone;

    @Column(name = "cor", length = 20)
    private String cor;

    @Column(name = "perfil_financeiro", length = 30)
    private String perfilFinanceiro;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
