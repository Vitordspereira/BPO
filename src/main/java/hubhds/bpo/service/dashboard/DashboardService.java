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

        Categoria categoria = buscarCategoriaPorNome(
                dashboardRequest.categoria(),
                usuario.getIdUsuario(),
                dashboardRequest.perfilFinanceiro()
        );

        if (!categoria.getTipo().equals(dashboardRequest.tipo())) {
            throw new RuntimeException("O tipo da categoria é diferente do tipo de lançamento");
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

        if (isMeioPagamentoCartao(dashboardRequest.meioPagamento())) {
            Cartao cartao = cartaoRepository.findById(dashboardRequest.idCartao())
                    .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

            if (!cartao.getUsuario().getIdUsuario().equals(idUsuario)) {
                throw new RuntimeException("Este cartão não pertence a este usuário!");
            }

            if (cartao.getPerfilFinanceiro() != dashboardRequest.perfilFinanceiro()) {
                throw new RuntimeException("O cartão não pertence ao mesmo perfil financeiro");
            }

            dashboard.setCartao(cartao);
        } else {
            dashboard.setCartao(null);
        }

        dashboardRepository.save(dashboard);
        return new DashboardResponse(dashboard);
    }

    private void validarMeioPagamento(DashboardRequest dashboardRequest) {
        if (dashboardRequest.tipo() == Tipo.DESPESA && dashboardRequest.meioPagamento() == null) {
            throw new RuntimeException("Meio de pagamento é obrigatório para despesas.");
        }

        if (isMeioPagamentoCartao(dashboardRequest.meioPagamento()) && dashboardRequest.idCartao() == null) {
            throw new RuntimeException("Cartão é obrigatório para pagamentos com cartão.");
        }
    }

    // Metodo privado para validar se o usuário pode realizar lançamentos
    // Regra atual implementada no seu projeto
    private void validarAcessoUsuario(Usuario usuario) {
        if (!usuario.getAssinaturaAtiva()) {
            if (usuario.getDataInatividade() != null) {
                long diasInativo = ChronoUnit.DAYS.between(
                        usuario.getDataInatividade(),
                        java.time.LocalDateTime.now()
                );

                if (diasInativo > 30) {
                    throw new RuntimeException("Acesso negado: Assinatura suspensa a mais de 30 dias.");
                } else {
                    throw new RuntimeException("Acesso negado: Regularize a sua assinatura.");
                }
            }
        }
    }

    private boolean isMeioPagamentoCartao(MeioPagamento meioPagamento) {
        return meioPagamento == MeioPagamento.CREDITO
                || meioPagamento == MeioPagamento.DEBITO;
    }

    private Categoria buscarCategoriaPorNome(String nomeCategoria, Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        return categoriaRepository.findByUsuario_IdUsuarioAndPerfilFinanceiroOrderByNomeAsc(idUsuario, perfilFinanceiro)
                .stream()
                .filter(categoria -> normalizar(categoria.getNome()).equals(normalizar(nomeCategoria)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoria inválida para este usuário."));
    }

    private String normalizar(String texto) {
        if (texto == null) return "";

        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }

    public List<Object[]> buscarDadosGraficoPizza(Long idUsuario, PerfilFinanceiro perfilFinanceiro, Tipo tipo) {
        return dashboardRepository.buscarPorCategoria(idUsuario, perfilFinanceiro, tipo);
    }

    public List<Object[]> buscarTotaisCards(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        return dashboardRepository.buscarResumoFinanceiro(idUsuario, perfilFinanceiro);
    }

    public DashboardResumoDTO buscarResumo(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        List<Dashboard> lancamento = dashboardRepository.findByUsuarioIdUsuarioAndPerfilFinanceiro(idUsuario, perfilFinanceiro);

        BigDecimal entradas = lancamento.stream()
                .filter(d -> d.getTipo().name().equals("RECEITA"))
                .map(Dashboard::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saidas = lancamento.stream()
                .filter(d -> d.getTipo().name().equals("DESPESA"))
                .map(Dashboard::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoFinal = entradas.subtract(saidas);

        return new DashboardResumoDTO(entradas, saidas, saldoFinal, lancamento);
    }

    @Transactional
    public DashboardResponse atualizar(Long idUsuario, Long idDashboard, DashboardRequest dashboardRequest) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validarAcessoUsuario(usuario);

        Dashboard dashboard = dashboardRepository.findById(idDashboard)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!dashboard.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Você não tem permissão para editar essa transação");
        }

        Categoria categoria = buscarCategoriaPorNome(
                dashboardRequest.categoria(),
                usuario.getIdUsuario(),
                dashboardRequest.perfilFinanceiro()
        );

        if (!categoria.getTipo().equals(dashboardRequest.tipo())) {
            throw new RuntimeException("A categoria é diferente do lançamento");
        }

        validarMeioPagamento(dashboardRequest);

        dashboard.setCategoria(categoria);
        dashboard.setDescricao(dashboardRequest.descricao());
        dashboard.setValor(dashboardRequest.valor());
        dashboard.setData(dashboardRequest.data());
        dashboard.setTipo(dashboardRequest.tipo());
        dashboard.setMeioPagamento(dashboardRequest.meioPagamento());
        dashboard.setPerfilFinanceiro(dashboardRequest.perfilFinanceiro());

        if (isMeioPagamentoCartao(dashboardRequest.meioPagamento())) {
            Cartao cartao = cartaoRepository.findById(dashboardRequest.idCartao())
                    .orElseThrow(() -> new RuntimeException("cartão não encontrado"));

            if (!cartao.getUsuario().getIdUsuario().equals(idUsuario)) {
                throw new RuntimeException("Esse cartão pertence a este usuário");
            }

            if (cartao.getPerfilFinanceiro() != dashboardRequest.perfilFinanceiro()) {
                throw new RuntimeException("O cartão não pertence a esse perfil financeiro");
            }

            dashboard.setCartao(cartao);
        } else {
            dashboard.setCartao(null);
        }

        dashboardRepository.save(dashboard);
        return new DashboardResponse(dashboard);
    }

    @Transactional
    public void deletar(Long idUsuario, Long idDashboard) {
        Dashboard dashboard = dashboardRepository.findById(idDashboard)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!dashboard.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Não tem permissão para excluir essa transação");
        }

        dashboardRepository.delete(dashboard);
    }
}