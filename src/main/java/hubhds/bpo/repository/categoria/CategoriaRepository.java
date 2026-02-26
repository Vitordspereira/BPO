package hubhds.bpo.repository.categoria;

import hubhds.bpo.model.categoria.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    //lista todas as categorias de cada usuário
    List<Categoria> findByCadastro_IdCadastro(Long idCadastro);
}
