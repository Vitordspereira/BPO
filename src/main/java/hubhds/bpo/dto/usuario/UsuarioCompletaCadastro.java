package hubhds.bpo.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCompletaCadastro(

        @NotBlank(message = "Nome completo é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "EW-mail inválido")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        String telefone,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {
}
