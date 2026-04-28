package hubhds.bpo.dto.categoria.categoriaComLancamento;

import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.usuario.PerfilFinanceiro;


public record CategoriaComLancamento(
        String categoria,
        Tipo movimentacao,
        PerfilFinanceiro tipoGasto,
        String icone,
        String cor
) {
}
