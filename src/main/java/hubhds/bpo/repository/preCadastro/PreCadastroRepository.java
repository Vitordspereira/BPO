package hubhds.bpo.repository.preCadastro;

import hubhds.bpo.model.preCadastro.PreCadastro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreCadastroRepository extends JpaRepository<PreCadastro, Long> {

    Optional<PreCadastro> findByToken(String token);

    Optional<PreCadastro> findByMpPreapprovalId(String mpPreapprovalId);

    Optional<PreCadastro> findByMpExternalReference(String mpExternalReference);
}
