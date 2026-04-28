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
        // Regex explicações:
        // ^55 -> Começa obrigatoriamente com 55
        // [1-9]{2} -> DDD (dois dígitos de 1 a 9)
        // [2-9] -> Primeiro dígito do número (não pode começar com 0 ou 1)
        // [0-9]{7,8} -> Restante do número (7 dígitos para fixo ou 8 para celular)
        @Size(min = 12, max = 20)
        @Pattern(
                regexp ="^55[1-9]{2}[2-9][0-9]{7,8}",
                message = "O número de telefone deve ser completo"
        )
        String telefone,

        @NotBlank(message = "Senha é obrigatória.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula, minúscula, número e caractere especial."
        )
        @Size(max = 100)
        String senha,

        @NotBlank(message = "Confirmação de senha é obrigatória.")
        String confirmarSenha
        ) {
}
