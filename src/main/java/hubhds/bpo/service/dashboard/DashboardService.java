package hubhds.bpo.service.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.dto.dashboard.resumo.DashboardResumoDTO;
import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.dashboard.Dashboard;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.cartao.CartaoRepository;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import hubhds.bpo.repository.dashboard.DashboardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Transactional
    public DashboardResponse salvar(Long idUsuario, DashboardRequest dashboardRequest) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validarAcessoUsuario(usuario);
        validarRequest(dashboardRequest);

        Categoria categoria = buscarCategoriaPorNome(
                dashboardRequest.categoria(),
                usuario.getIdUsuario(),
                dashboardRequest.perfilFinanceiro()
        );

        if (categoria.getTipo() != dashboardRequest.tipo()) {
            throw new RuntimeException("O tipo da categoria é diferente do tipo de lançamento.");
        }

        validarMeioPagamento(dashboardRequest);

        Dashboard dashboard = new Dashboard();
        dashboard.setUsuario(usuario);
        dashboard.setCategoria(categoria);
        dashboard.setDescricao(dashboardRequest.descricao());
        dashboard.setValor(dashboardRequest.valor());
        dashboard.setData(dashboardRequest.data());
        dashboard.setTipo(dashboardRequest.tipo());
        dashboard.setMeioPagamento(dashboardRequest.meioPagamento());
        dashboard.setPerfilFinanceiro(dashboardRequest.perfilFinanceiro());

        vincularCartaoSeNecessario(dashboard, dashboardRequest, idUsuario);

        dashboardRepository.save(dashboard);

        return new DashboardResponse(dashboard);
    }

    @Transactional
    public DashboardResponse atualizar(Long idUsuario, Long idDashboard, DashboardRequest dashboardRequest) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validarAcessoUsuario(usuario);
        validarRequest(dashboardRequest);

        Dashboard dashboard = dashboardRepository.findById(idDashboard)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!Objects.equals(dashboard.getUsuario().getIdUsuario(), idUsuario)) {
            throw new RuntimeException("Você não tem permissão para editar essa transação.");
        }

        Categoria categoria = buscarCategoriaPorNome(
                dashboardRequest.categoria(),
                usuario.getIdUsuario(),
                dashboardRequest.perfilFinanceiro()
        );

        if (categoria.getTipo() != dashboardRequest.tipo()) {
            throw new RuntimeException("A categoria é diferente do lançamento.");
        }

        validarMeioPagamento(dashboardRequest);

        dashboard.setCategoria(categoria);
        dashboard.setDescricao(dashboardRequest.descricao());
        dashboard.setValor(dashboardRequest.valor());
        dashboard.setData(dashboardRequest.data());
        dashboard.setTipo(dashboardRequest.tipo());
        dashboard.setMeioPagamento(dashboardRequest.meioPagamento());
        dashboard.setPerfilFinanceiro(dashboardRequest.perfilFinanceiro());

        vincularCartaoSeNecessario(dashboard, dashboardRequest, idUsuario);

        dashboardRepository.save(dashboard);

        return new DashboardResponse(dashboard);
    }

    @Transactional
    public void deletar(Long idUsuario, Long idDashboard) {
        Dashboard dashboard = dashboardRepository.findById(idDashboard)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!Objects.equals(dashboard.getUsuario().getIdUsuario(), idUsuario)) {
            throw new RuntimeException("Não tem permissão para excluir essa transação.");
        }

        dashboardRepository.delete(dashboard);
    }

    public List<Object[]> buscarDadosGraficoPizza(Long idUsuario, PerfilFinanceiro perfilFinanceiro, Tipo tipo) {
        return dashboardRepository.buscarPorCategoria(idUsuario, perfilFinanceiro, tipo);
    }

    public List<Object[]> buscarTotaisCards(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        return dashboardRepository.buscarResumoFinanceiro(idUsuario, perfilFinanceiro);
    }

    public DashboardResumoDTO buscarResumo(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        List<Dashboard> lancamentos = dashboardRepository.findByUsuarioIdUsuarioAndPerfilFinanceiro(
                idUsuario,
                perfilFinanceiro
        );

        BigDecimal entradas = lancamentos.stream()
                .filter(d -> d.getTipo() == Tipo.RECEITA)
                .map(Dashboard::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidas = lancamentos.stream()
                .filter(d -> d.getTipo() == Tipo.DESPESA)
                .map(Dashboard::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFinal = entradas.subtract(saidas);

        return new DashboardResumoDTO(entradas, saidas, saldoFinal, lancamentos);
    }

    private void validarRequest(DashboardRequest dashboardRequest) {
        if (dashboardRequest == null) {
            throw new RuntimeException("Dados do lançamento não enviados.");
        }

        if (dashboardRequest.categoria() == null || dashboardRequest.categoria().isBlank()) {
            throw new RuntimeException("Categoria é obrigatória.");
        }

        if (dashboardRequest.tipo() == null) {
            throw new RuntimeException("Tipo do lançamento é obrigatório.");
        }

        if (dashboardRequest.valor() == null) {
            throw new RuntimeException("Valor é obrigatório.");
        }

        if (dashboardRequest.data() == null) {
            throw new RuntimeException("Data é obrigatória.");
        }

        if (dashboardRequest.perfilFinanceiro() == null) {
            throw new RuntimeException("Perfil financeiro é obrigatório.");
        }
    }

    private void validarMeioPagamento(DashboardRequest dashboardRequest) {
        if (dashboardRequest.tipo() == Tipo.DESPESA && dashboardRequest.meioPagamento() == null) {
            throw new RuntimeException("Meio de pagamento é obrigatório para despesas.");
        }

        /*
         * Importante:
         * Antes o sistema obrigava idCartao sempre que fosse crédito/débito.
         * Mas o front também trabalha com cartões/lançamentos automáticos.
         * Por isso, agora só validamos o cartão se o idCartao vier preenchido.
         */
    }

    private void vincularCartaoSeNecessario(
            Dashboard dashboard,
            DashboardRequest dashboardRequest,
            Long idUsuario
    ) {
        if (!isMeioPagamentoCartao(dashboardRequest.meioPagamento())) {
            dashboard.setCartao(null);
            return;
        }

        if (dashboardRequest.idCartao() == null) {
            dashboard.setCartao(null);
            return;
        }

        Cartao cartao = cartaoRepository.findById(dashboardRequest.idCartao())
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado."));

        if (!Objects.equals(cartao.getUsuario().getIdUsuario(), idUsuario)) {
            throw new RuntimeException("Este cartão não pertence a este usuário.");
        }

        if (cartao.getPerfilFinanceiro() != dashboardRequest.perfilFinanceiro()) {
            throw new RuntimeException("O cartão não pertence ao mesmo perfil financeiro.");
        }

        dashboard.setCartao(cartao);
    }

    private void validarAcessoUsuario(Usuario usuario) {
        Boolean assinaturaAtiva = usuario.getAssinaturaAtiva();

        if (Boolean.TRUE.equals(assinaturaAtiva)) {
            return;
        }

        if (usuario.getDataInatividade() == null) {
            throw new RuntimeException("Acesso negado: Regularize a sua assinatura.");
        }

        long diasInativo = ChronoUnit.DAYS.between(
                usuario.getDataInatividade(),
                LocalDateTime.now()
        );

        if (diasInativo > 30) {
            throw new RuntimeException("Acesso negado: Assinatura suspensa há mais de 30 dias.");
        }

        throw new RuntimeException("Acesso negado: Regularize a sua assinatura.");
    }

    private boolean isMeioPagamentoCartao(MeioPagamento meioPagamento) {
        return meioPagamento != null && meioPagamento.isCartao();
    }

    private Categoria buscarCategoriaPorNome(
            String nomeCategoria,
            Long idUsuario,
            PerfilFinanceiro perfilFinanceiro
    ) {
        return categoriaRepository
                .findByUsuario_IdUsuarioAndPerfilFinanceiroOrderByNomeAsc(idUsuario, perfilFinanceiro)
                .stream()
                .filter(categoria -> normalizar(categoria.getNome()).equals(normalizar(nomeCategoria)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoria inválida para este usuário."));
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}