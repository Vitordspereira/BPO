package hubhds.bpo.dto.cartao;

import hubhds.bpo.model.cartao.Bandeira;
import hubhds.bpo.model.cartao.Categoria;

import java.math.BigDecimal;

public record CartaoDTO(
        String nomeCartao,
        String numeroMascara,
        Integer diaFechamento,
        Integer diaVencimento,
        Bandeira bandeira,
        Categoria categoria,
        BigDecimal limiteTotal,
        Long idUsuario
) {
}
