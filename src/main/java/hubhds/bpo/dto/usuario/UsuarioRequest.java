package hubhds.bpo.dto.usuario;

import jakarta.validation.constraints.*;

public record UsuarioRequest(

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150)
        String nomeCompleto,

        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Telefone é obrigatório.")
        @Size(max = 20)
        String telefone,

        @NotBlank(message = "Senha é obrigatória.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial."
        )
        @Size(max = 100)
        String senha,

        @NotBlank(message = "Confirmação de senha é obrigatória.")
        String confirmarSenha,

        @NotBlank(message = "CPF é obrigatório.")
        @Size(min = 11, max = 14, message = "CPF inválido.")
        String cpf,

        @NotNull(message = "Informe se possui empresa.")
        Integer temEmpresa,

        @Size(min = 14, max = 18, message = "CNPJ inválido.")
        String cnpj
) {
}
