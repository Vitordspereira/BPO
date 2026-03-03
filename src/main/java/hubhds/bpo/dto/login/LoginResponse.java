package hubhds.bpo.dto.login;

public record LoginResponse(
        Long idUsuario,
        String nomeCompleto,
        String email,
        String token,
        Integer temEmpresa,
        String cnpj
) {
}
