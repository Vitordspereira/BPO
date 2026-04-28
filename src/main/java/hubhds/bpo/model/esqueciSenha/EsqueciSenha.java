package hubhds.bpo.model.esqueciSenha;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "esqueci_senha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsqueciSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reset")
    private Long idReset;

    @Column( name = "token", nullable = false, unique = true, length = 180)
    private String token;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "expiracao", nullable = false)
    private LocalDateTime expiracao;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}
