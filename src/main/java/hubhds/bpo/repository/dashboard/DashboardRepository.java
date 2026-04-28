package hubhds.bpo.repository.dashboard;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.dashboard.Dashboard;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    List<Dashboard> findByUsuarioIdUsuarioAndPerfilFinanceiro(Long idUsuario, PerfilFinanceiro perfilFinanceiro);

    //Gráfico pizza despesa e receita
    @Query("""
        SELECT d.categoria.nome, SUM(d.valor)
        FROM Dashboard d
        WHERE d.usuario.idUsuario = :idUsuario
          AND d.perfilFinanceiro = :perfilFinanceiro
          AND d.tipo = :tipo
        GROUP BY d.categoria.nome
        """)
    List<Object[]> buscarPorCategoria(@Param("idUsuario") Long idUsuario,
                                      @Param("perfilFinanceiro") PerfilFinanceiro perfilFinanceiro,
                                      @Param("tipo")Tipo tipo);

    @Query("""
        SELECT d.tipo, SUM(d.valor)
        FROM Dashboard d
        WHERE d.usuario.idUsuario = :idUsuario
          AND d.perfilFinanceiro = :perfilFinanceiro
        GROUP BY d.tipo
        """)
    List<Object[]> buscarResumoFinanceiro(@Param("idUsuario") Long idUsuario,
                                          @Param("perfilFinanceiro") PerfilFinanceiro perfilFinanceiro);

    @Modifying
    @Transactional
    @Query("delete from Dashboard d where d.cartao.idCartao = :idCartao")
    void deleteByCartao_IdCartao(@Param("idCartao") Long idCartao);

    @Modifying
    @Transactional
    @Query("delete from Dashboard d where d.categoria.idCategoria =:idCategoria")
    void deleteByCategoria_IdCategoria(@Param("idCategoria")Long idCategoria);
}