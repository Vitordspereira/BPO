package hubhds.bpo.service.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.dto.categoria.CategoriaUnificadaResponse;
import hubhds.bpo.dto.categoria.categoriaComLancamento.CategoriaComLancamento;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import hubhds.bpo.repository.categorian8n.CategoriaN8nRepository;
import hubhds.bpo.repository.dashboard.DashboardRepository;
import hubhds.bpo.repository.lancamento.LancamentoRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // NOVO:
    // Esse repository permite buscar as categorias criadas pelo WhatsApp/N8N.
    // É daqui que virá a categoria "Automatica".
    @Autowired
    private CategoriaN8nRepository categoriaN8nRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    public CategoriaResponse salvar(Long idUsuario, CategoriaRequest categoriaRequest) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String slug = gerarSlug(categoriaRequest.nome());

        if (slug == null) {
            throw new RuntimeException("Nome da categoria é obrigatório");
        }

        boolean categoriaJaExiste =
                categoriaRepository.existsByUsuario_IdUsuarioAndPerfilFinanceiroAndSlug(
                        idUsuario,
                        categoriaRequest.perfilFinanceiro(),
                        slug
                );

        if (categoriaJaExiste) {
            throw new RuntimeException("Já existe uma categoria com esse nome no perfil");
        }

        Categoria categoria = new Categoria();
        categoria.setUsuario(usuario);
        categoria.setNome(categoriaRequest.nome());
        categoria.setSlug(slug);
        categoria.setTipo(categoriaRequest.tipo());
        categoria.setIcone(categoriaRequest.icone());
        categoria.setCor(categoriaRequest.cor());
        categoria.setPerfilFinanceiro(categoriaRequest.perfilFinanceiro());

        categoria = categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }

    // NOVO:
    // Esse método foi alterado para trazer categorias de dois lugares:
    //
    // 1. Tabela "categoria":
    //    categorias criadas manualmente pelo usuário dentro do projeto.
    //
    // 2. Tabela "categoria_n8n":
    //    categorias criadas automaticamente pelo WhatsApp/N8N,
    //    incluindo a categoria "Automatica".
    //
    // A rota continua usando idUsuario e perfilFinanceiro.
    // Exemplo:
    // /categoria/listar/1?perfilFinanceiro=PESSOAL
    public List<CategoriaUnificadaResponse> listarPorUsuario(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // NOVO:
        // Busca as categorias normais do projeto.
        // Essas são as categorias que o usuário criou manualmente.
        List<CategoriaUnificadaResponse> categoriasProjeto =
                categoriaRepository.findByUsuario_IdUsuarioAndPerfilFinanceiroOrderByNomeAsc(
                                idUsuario,
                                perfilFinanceiro
                        )
                        .stream()
                        .map(categoria -> new CategoriaUnificadaResponse(
                                categoria.getIdCategoria(),
                                categoria.getNome(),
                                categoria.getTipo() != null ? categoria.getTipo().name() : null,
                                categoria.getIcone(),
                                categoria.getCor(),
                                categoria.getPerfilFinanceiro() != null ? categoria.getPerfilFinanceiro().name() : null,
                                "PROJETO"
                        ))
                        .toList();

        // NOVO:
        // Aqui será montada a lista de categorias vindas do N8N.
        // Começa vazia porque pode existir usuário sem telefone cadastrado.
        List<CategoriaUnificadaResponse> categoriasN8n = new ArrayList<>();

        // NOVO:
        // As categorias do N8N são vinculadas pelo telefone.
        // Então pegamos o telefone do usuário para procurar em categoria_n8n.
        if (usuario.getTelefone() != null && !usuario.getTelefone().isBlank()) {
            categoriasN8n =
                    categoriaN8nRepository.findByTelefoneOrderByNomeAsc(usuario.getTelefone())
                            .stream()

                            // NOVO:
                            // Garante que vamos listar apenas categorias do mesmo perfil financeiro.
                            // Se a tela pediu PESSOAL, só entram categorias N8N PESSOAL.
                            // Se a tela pediu EMPRESA, só entram categorias N8N EMPRESA.
                            .filter(categoria -> categoria.getPerfilFinanceiro() != null)
                            .filter(categoria -> categoria.getPerfilFinanceiro().equalsIgnoreCase(perfilFinanceiro.name()))

                            // NOVO:
                            // Converte CategoriaN8n para CategoriaUnificadaResponse.
                            // A origem "N8N" serve para o front saber que essa categoria veio do WhatsApp.
                            .map(categoria -> new CategoriaUnificadaResponse(
                                    categoria.getIdCategoriaN8n(),
                                    categoria.getNome(),
                                    categoria.getTipo(),
                                    categoria.getIcone(),
                                    categoria.getCor(),
                                    categoria.getPerfilFinanceiro(),
                                    "N8N"
                            ))
                            .toList();
        }

        // NOVO:
        // Junta as duas listas:
        // categorias criadas no projeto + categorias vindas do N8N.
        //
        // É aqui que a "Automatica" passa a aparecer junto com as demais.
        List<CategoriaUnificadaResponse> resultado = new ArrayList<>();
        resultado.addAll(categoriasProjeto);
        resultado.addAll(categoriasN8n);

        return resultado;
    }

    public CategoriaResponse atualizar(Long idUsuario, Long idCategoria, CategoriaRequest dto) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));

        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Permissão negada: você não é o dono desta categoria!");
        }

        String slug = gerarSlug(dto.nome());

        if (slug == null) {
            throw new RuntimeException("Nome da categoria é obrigatório");
        }

        boolean categoriaJaExiste =
                categoriaRepository.existsByUsuario_IdUsuarioAndPerfilFinanceiroAndSlug(
                        idUsuario,
                        dto.perfilFinanceiro(),
                        slug
                );

        if (categoriaJaExiste && !slug.equals(categoria.getSlug())) {
            throw new RuntimeException("Já existe uma categoria com esse nome no perfil");
        }

        categoria.setNome(dto.nome());
        categoria.setSlug(slug);
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());
        categoria.setPerfilFinanceiro(dto.perfilFinanceiro());

        categoria = categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }

    @Transactional
    public CategoriaResponse atualizarPorReferencia(Long idUsuario, String categoriaRef, CategoriaRequest dto) {
        Categoria categoria;

        if (categoriaRef.matches("\\d+")) {
            Long idCategoria = Long.valueOf(categoriaRef);

            categoria = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        } else {
            categoria = categoriaRepository.findByUsuario_IdUsuarioAndSlug(idUsuario, categoriaRef)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        }

        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Permissão negada: você não é o dono desta categoria!");
        }

        String slug = gerarSlug(dto.nome());

        if (slug == null) {
            throw new RuntimeException("Nome da categoria é obrigatório");
        }

        boolean categoriaJaExiste =
                categoriaRepository.existsByUsuario_IdUsuarioAndPerfilFinanceiroAndSlug(
                        idUsuario,
                        dto.perfilFinanceiro(),
                        slug
                );

        if (categoriaJaExiste && !slug.equals(categoria.getSlug())) {
            throw new RuntimeException("Já existe uma categoria com esse nome no perfil");
        }

        categoria.setNome(dto.nome());
        categoria.setSlug(slug);
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());
        categoria.setPerfilFinanceiro(dto.perfilFinanceiro());

        categoria = categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }

    @Transactional
    public void deletar(Long idUsuario, Long idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));

        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Não tem permissão para excluir essa categoria");
        }

        dashboardRepository.deleteByCategoria_IdCategoria(idCategoria);
        lancamentoRepository.deleteByCategoriaIdCategoria(idCategoria);
        categoriaRepository.delete(categoria);
    }

    private String gerarSlug(String nome) {
        if (nome == null) return null;

        String texto = nome.trim();
        if (texto.isEmpty()) return null;

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        return "cat_" + texto;
    }

    private PerfilFinanceiro converterTipoGasto(PerfilFinanceiro tipoGasto) {
        return tipoGasto;
    }

    /**
     * Mantido por compatibilidade com o front antigo.
     * Agora esse metodo cria apenas a categoria.
     * A transação/lançamento deve ser criada em endpoint separado.
     */
    @Transactional
    public CategoriaResponse salvarLancamento(Long idUsuario, CategoriaComLancamento categoriaComLancamento) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String slug = gerarSlug(categoriaComLancamento.categoria());

        if (slug == null) {
            throw new RuntimeException("Nome de categoria é obrigatório");
        }

        PerfilFinanceiro perfilFinanceiro = converterTipoGasto(categoriaComLancamento.tipoGasto());

        boolean categoriaJaExiste =
                categoriaRepository.existsByUsuario_IdUsuarioAndPerfilFinanceiroAndSlug(
                        idUsuario,
                        perfilFinanceiro,
                        slug
                );

        if (categoriaJaExiste) {
            throw new RuntimeException("Já existe uma categoria com esse nome no perfil");
        }

        Categoria categoria = new Categoria();
        categoria.setUsuario(usuario);
        categoria.setNome(categoriaComLancamento.categoria());
        categoria.setSlug(slug);
        categoria.setTipo(categoriaComLancamento.movimentacao());
        categoria.setIcone(categoriaComLancamento.icone());
        categoria.setCor(categoriaComLancamento.cor());
        categoria.setPerfilFinanceiro(perfilFinanceiro);

        categoria = categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }
}