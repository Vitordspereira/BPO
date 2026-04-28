package hubhds.bpo.dto.categoria;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequest(

        @NotBlank(message = "Informe o nome da categoria")
        String nome,

        @NotNull
        Tipo tipo,

        String icone,
        String cor,
        PerfilFinanceiro perfilFinanceiro
) {
}
