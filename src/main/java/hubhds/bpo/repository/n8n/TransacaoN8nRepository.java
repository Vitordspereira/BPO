package hubhds.bpo.repository.n8n;

import hubhds.bpo.model.n8n.N8n;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoN8nRepository extends JpaRepository<N8n, String> {

    List<N8n> findByTelefoneAndDataTransacaoBetweenOrderByDataTransacaoDescTransactionIdDesc(String telefone, LocalDate dataInicio, LocalDate dataFim);
}
