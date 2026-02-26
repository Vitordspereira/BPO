package hubhds.bpo.repository.cadastro;

import hubhds.bpo.model.cadastro.Cadastro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CadastroRepository extends JpaRepository<Cadastro, Long> {

    //Login
    Optional<Cadastro> findByEmail(String email);
}