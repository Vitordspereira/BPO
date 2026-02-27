package hubhds.bpo.service.login;

import hubhds.bpo.dto.login.LoginRequest;
import hubhds.bpo.dto.login.LoginResponse;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req) {
        Usuario user = usuarioRepository.findByTelefone(req.email())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(req.senha(), user.getSenha())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        return new LoginResponse(
                user.getIdUsuario(),
                user.getNomeCompleto(),
                user.getEmail(),
                user.getTemEmpresa(),
                user.getCnpj()
        );
    }
}
