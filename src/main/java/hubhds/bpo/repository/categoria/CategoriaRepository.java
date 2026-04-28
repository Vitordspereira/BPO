package hubhds.bpo.repository.categoria;

import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByUsuario_IdUsuarioAndPerfilFinanceiro(
            Long idUsuario,
            PerfilFinanceiro perfilFinanceiro
    );

    List<Categoria> findByUsuario_IdUsuarioAndPerfilFinanceiroOrderByNomeAsc(
            Long idUsuario,
            PerfilFinanceiro perfilFinanceiro
    );

    Optional<Categoria> findByUsuario_IdUsuarioAndSlug(
            Long idUsuario,
            String slug
    );

    boolean existsByUsuario_IdUsuarioAndPerfilFinanceiroAndSlug(
            Long idUsuario,
            PerfilFinanceiro perfilFinanceiro,
            String slug
    );
}
