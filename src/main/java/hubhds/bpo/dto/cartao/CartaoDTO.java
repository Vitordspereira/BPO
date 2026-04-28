package hubhds.bpo.dto.cartao;

import hubhds.bpo.model.cartao.Bandeira;
import hubhds.bpo.model.cartao.Categoria;
import hubhds.bpo.model.cartao.StatusCartao;
import hubhds.bpo.model.usuario.PerfilFinanceiro;

import java.math.BigDecimal;

public record CartaoDTO(
        String nomeCartao,
        String numeroMascara,
        Integer diaFechamento,
        Integer diaVencimento,
        Bandeira bandeira,
        Categoria categoria,
        BigDecimal limiteTotal,
        StatusCartao statusCartao,
        Long idUsuario,
        String icone,
        String cor,
        PerfilFinanceiro perfilFinanceiro
) {
}
