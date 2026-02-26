package hubhds.bpo.repository.cartao;

import hubhds.bpo.model.cartao.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    Optional<Cartao> findByNomeCartao(String nomeCartao);
}
