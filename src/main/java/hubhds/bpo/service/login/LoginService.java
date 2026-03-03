package hubhds.bpo.service.login;

import hubhds.bpo.dto.login.LoginRequest;
import hubhds.bpo.dto.login.LoginResponse;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.service.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest req) {
        //Busca o usuário pelo email
        Usuario user = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));

        if (user.getSenha() == null){
            throw new RuntimeException("Sua senha não foi cadastrada, verifique seu email!");
        }

        //Verifica se a senha
        if (!passwordEncoder.matches(req.senha(), user.getSenha())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        //Bloqueia login se assinatura estiver inativa
        if (!user.getAssinaturaAtiva()) {
            throw new RuntimeException("Sua assinatura está inativa, verifique se o pagamento foi realizado!");
        }

        String token = tokenService.gerarToken(user);

        return new LoginResponse(
                user.getIdUsuario(),
                user.getNomeCompleto(),
                user.getEmail(),
                token,
                user.getTemEmpresa(),
                user.getCnpj()
        );
    }
}
