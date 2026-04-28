package hubhds.bpo.service.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.dto.categoria.categoriaComLancamento.CategoriaComLancamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.repository.lancamento.LancamentoRepository;
import hubhds.bpo.repository.dashboard.DashboardRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

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

    public List<CategoriaResponse> listarPorUsuario(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        return categoriaRepository.findByUsuario_IdUsuarioAndPerfilFinanceiro(idUsuario, perfilFinanceiro)
                .stream()
                .map(CategoriaResponse::new)
                .collect(Collectors.toList());
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