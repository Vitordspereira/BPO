package hubhds.bpo.repository.usuario;

import hubhds.bpo.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByTelefone(String telefone);

    //Login
    Optional<Usuario> findByEmail(String email);

    //Localizar usuário pela assinatura do mercado pago
    Optional<Usuario> findByMpPreapprovalId(String mpPreapprovalId);

    //Localizar usuário pela referência interna enviada no Mercado Pago
    Optional<Usuario> findByMpExternalReference(String mpExternalReference);
}

