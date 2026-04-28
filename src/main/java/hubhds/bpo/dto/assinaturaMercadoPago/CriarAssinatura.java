package hubhds.bpo.dto.assinaturaMercadoPago;

import jakarta.validation.constraints.NotBlank;

//Isso aqui é para teste
public record CriarAssinatura(

        @NotBlank(message = "Informe o telefone")
        String telefone,

        String plano,

        String payerEmail
) {
}
