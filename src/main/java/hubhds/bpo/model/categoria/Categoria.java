package hubhds.bpo.model.categoria;

import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @NotBlank
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "slug", length = 100, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private Tipo tipo;

    @Column(name = "icone", length = 50)
    private String icone;

    @Column(name = "cor", length = 20)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_financeiro", nullable = false)
    private PerfilFinanceiro perfilFinanceiro;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
