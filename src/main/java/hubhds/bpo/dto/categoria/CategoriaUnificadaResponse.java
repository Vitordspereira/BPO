package hubhds.bpo.dto.categoria;

public record CategoriaUnificadaResponse(
        Long id,
        String nome,
        String tipo,
        String icone,
        String cor,
        String perfilFinanceiro,
        String origem
) {
}
