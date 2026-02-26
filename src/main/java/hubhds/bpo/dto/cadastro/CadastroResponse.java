package hubhds.bpo.dto.cadastro;

public record CadastroResponse(
        Long idCadastro,
        String nomeCompleto,
        String email,
        String telefone,
        String cpf,
        Integer  temEmpresa,
        String cnpj,
        Boolean assinaturaAtiva
) {}

