package hubhds.bpo.dto.usuario.perfil;

import hubhds.bpo.model.usuario.AmbienteUsuario;

public record PerfilDTO(
        String nomeCompleto,
        String email,
        String telefone,
        AmbienteUsuario ambienteUsuario,
        Integer temEmpresa
) {
}
