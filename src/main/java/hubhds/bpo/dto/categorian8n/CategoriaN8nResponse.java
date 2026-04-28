package hubhds.bpo.dto.categorian8n;

import hubhds.bpo.model.categorian8n.CategoriaN8n;

public record CategoriaN8nResponse(
        Long idCategoriaN8n,
        String telefone,
        String nome,
        String tipo,
        String icone,
        String cor,
        String perfilFinanceiro
) {
    public CategoriaN8nResponse(CategoriaN8n categoriaN8n) {
        this(
                categoriaN8n.getIdCategoriaN8n(),
                categoriaN8n.getTelefone(),
                categoriaN8n.getNome(),
                categoriaN8n.getTipo(),
                categoriaN8n.getIcone(),
                categoriaN8n.getCor(),
                categoriaN8n.getPerfilFinanceiro()
        );
    }
}
