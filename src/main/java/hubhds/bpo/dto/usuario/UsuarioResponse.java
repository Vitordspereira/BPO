package hubhds.bpo.dto.usuario;

public record UsuarioResponse(
        Long idUsuario,
        String nomeCompleto,
        String email,
        String telefone,
        Boolean assinaturaAtiva
) {}

