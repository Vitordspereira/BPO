package hubhds.bpo.dto.categoria;

public record CategoriaUnificadaResponse(
        Long idCategoria,
        Long idCategoriaN8n,
        String nome,
        String tipo,
        String icone,
        String cor,
        String perfilFinanceiro,
        String origem
) {
}
