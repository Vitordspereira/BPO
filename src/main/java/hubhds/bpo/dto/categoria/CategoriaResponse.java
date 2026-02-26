package hubhds.bpo.dto.categoria;

import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.categoria.Tipo;

public record CategoriaResponse(
        Long idCategoria,
        String nome,
        Tipo tipo,
        String icone,
        String cor
) {
    public CategoriaResponse(Categoria categoria) {
        this(
                categoria.getIdCategoria(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getIcone(),
                categoria.getCor()
        );
    }
}
