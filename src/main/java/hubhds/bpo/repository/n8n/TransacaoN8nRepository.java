package hubhds.bpo.repository.n8n;

import hubhds.bpo.model.n8n.N8n;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoN8nRepository extends JpaRepository<N8n, String> {

    List<N8n> findByTelefoneAndDataTransacaoBetweenOrderByDataTransacaoDescTransactionIdDesc(
            String telefone,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<N8n> findByTelefoneOrderByDataTransacaoDescTransactionIdDesc(String telefone);

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM N8n n
        WHERE n.telefone = :telefone
          AND LOWER(n.categoria) = LOWER(:categoria)
          AND LOWER(n.tipoGasto) = LOWER(:tipoGasto)
        """)
    int deleteTransacoesPorCategoriaPerfil(
            @Param("telefone") String telefone,
            @Param("categoria") String categoria,
            @Param("tipoGasto") String tipoGasto
    );
}