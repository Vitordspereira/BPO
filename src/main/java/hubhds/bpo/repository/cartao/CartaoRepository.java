package hubhds.bpo.repository.cartao;

import hubhds.bpo.model.cartao.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    @Query("SELECT c FROM Cartao c WHERE c.usuario.id = :idUsuario")
    List<Cartao> findByUsuario_IdUsuario(Long idUsuario);
}
