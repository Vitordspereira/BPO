package hubhds.bpo.dto.login;

public record LoginResponse(
        Long idCadastro,
        String nomeCompleto,
        String email,
        Integer temEmpresa,
        String cnpj
) {
}
