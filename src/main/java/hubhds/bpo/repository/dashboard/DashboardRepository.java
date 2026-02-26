package hubhds.bpo.repository.dashboard;

import hubhds.bpo.model.dashboard.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    //Para o gráfico de pizza: Soma por categoria
    @Query("SELECT d.categoria.nome, SUM(d.valor) FROM Dashboard d " +
            "WHERE d.cadastro.idCadastro = :idUsuario AND d.tipo = 'DESPESA' " +
            "GROUP BY d.categoria.nome")
    List<Object[]> somarDespesasPorCategoria(Long idUsuario);

    @Query("SELECT d.tipo, SUM(d.valor) FROM Dashboard d " +
            "WHERE d.cadastro.idCadastro = :idUsuario " +
            "GROUP BY d.tipo")
    List<Object[]> buscarResumoFinanceiro(Long idUsuario);
}
