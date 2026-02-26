package hubhds.bpo.service.login;

import hubhds.bpo.dto.login.LoginRequest;
import hubhds.bpo.dto.login.LoginResponse;
import hubhds.bpo.model.cadastro.Cadastro;
import hubhds.bpo.repository.cadastro.CadastroRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final CadastroRepository cadastroRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(CadastroRepository cadastroRepository, PasswordEncoder passwordEncoder) {
        this.cadastroRepository = cadastroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req) {
        Cadastro user = cadastroRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(req.senha(), user.getSenha())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        return new LoginResponse(
                user.getIdCadastro(),
                user.getNomeCompleto(),
                user.getEmail(),
                user.getTemEmpresa(),
                user.getCnpj()
        );
    }
}
