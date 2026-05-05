package hubhds.bpo.repository.n8n;

import hubhds.bpo.model.n8n.N8n;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}