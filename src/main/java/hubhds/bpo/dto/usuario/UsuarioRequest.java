package hubhds.bpo.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "Nome completo é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        String telefone,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "A senha deve conter no mínimo 8 caracteres")
        String senha
) {
}
