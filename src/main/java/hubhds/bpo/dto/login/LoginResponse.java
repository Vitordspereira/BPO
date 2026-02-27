package hubhds.bpo.dto.login;

public record LoginResponse(
        Long idUsuario,
        String nomeCompleto,
        String email,
        Integer temEmpresa,
        String cnpj
) {
}
