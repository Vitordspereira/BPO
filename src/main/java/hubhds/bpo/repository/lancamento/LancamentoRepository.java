package hubhds.bpo.repository.lancamento;

import hubhds.bpo.model.lancamento.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByCategoriaIdCategoria(Long idCategoria);

    List<Lancamento> findByUsuarioIdUsuario(Long idUsuario);

    void deleteByCategoriaIdCategoria(Long idCategoria);
}
