package hubhds.bpo.repository.categorian8n;

import hubhds.bpo.model.categorian8n.CategoriaN8n;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaN8nRepository extends JpaRepository<CategoriaN8n, Long> {

    List<CategoriaN8n> findByTelefoneOrderByNomeAsc(String telefone);

    Optional<CategoriaN8n> findByIdCategoriaN8nAndTelefone(
            Long idCategoriaN8n,
            String telefone
    );

    Optional<CategoriaN8n> findByTelefoneAndNomeIgnoreCase(
            String telefone,
            String nome
    );
}
