package hubhds.bpo.repository.esqueciSenha;

import hubhds.bpo.model.esqueciSenha.EsqueciSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EsqueciSenhaRepository extends JpaRepository<EsqueciSenha, Long> {

    Optional<EsqueciSenha> findByToken(String token);

    Optional<EsqueciSenha> findByEmailAndUsadoFalse(String email);
}
