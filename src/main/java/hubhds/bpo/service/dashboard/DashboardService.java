package hubhds.bpo.service.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.dto.dashboard.resumo.DashboardResumoDTO;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.dashboard.Dashboard;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.cartao.CartaoRepository;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import hubhds.bpo.repository.dashboard.DashboardRepository;
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

    public DashboardResponse salvar(Long idUsuario, DashboardRequest dashboardRequest) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // --- NOVA TRAVA DE SEGURANÇA PARA A EMILLY ---
        validarAcessoUsuario(usuario);
        // ---------------------------------------------

        Categoria categoria = categoriaRepository.findById(dashboardRequest.idCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Dashboard dashboard = new Dashboard();
        dashboard.setUsuario(usuario);
        dashboard.setCategoria(categoria);
        dashboard.setDescricao(dashboardRequest.descricao());
        dashboard.setValor(dashboardRequest.valor());
        dashboard.setData(dashboardRequest.data());
        dashboard.setTipo(dashboardRequest.tipo());
        dashboard.setMeioPagamento(dashboardRequest.meioPagamento());

        if (isMeioPagamentoCartao(dashboardRequest.meioPagamento()) && dashboardRequest.idCartao() != null) {
            Cartao cartao = cartaoRepository.findById(dashboardRequest.idCartao())
                    .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

            if (!cartao.getUsuario().getIdUsuario().equals(idUsuario)) {
                throw new RuntimeException("Este cartão não pertence a este usuário!");
            }
            dashboard.setCartao(cartao);
        }

        dashboardRepository.save(dashboard);
        return new DashboardResponse(dashboard);
    }


     //Metodo privado para validar se o usuário pode realizar lançamentos
     //Regra: Ativo ou Inativo há menos de 30 dias.
    private void validarAcessoUsuario(Usuario usuario){
        if (!usuario.getAssinaturaAtiva()) {
            if (usuario.getDataInatividade() != null) {
                long diasInativo = ChronoUnit.DAYS.between(
                        usuario.getDataInatividade(),
                        java.time.LocalDateTime.now()
                );

                if (diasInativo > 30) {
                    throw new RuntimeException("Acesso negado: Assinatura suspensa a mais de 30 dias.");
                } else {
                    //Se não tem assinatura ativa e nem data de inatividade, bloqueia
                    throw new RuntimeException("Acesso negado: Regularize a sua assinatura.");
                }
            }
        }
    }

    private boolean isMeioPagamentoCartao(MeioPagamento meioPagamento) {
        return meioPagamento == MeioPagamento.CARTAO_CREDITO || meioPagamento == MeioPagamento.CARTAO_DEBITO;
    }

    public List<Object[]> buscarDadosGraficoPizza(Long idUsuario) {
        return dashboardRepository.somarDespesasPorCategoria(idUsuario);
    }

    public List<Object[]> buscarTotaisCards(Long idUsuario) {
        return dashboardRepository.buscarResumoFinanceiro(idUsuario);
    }

    public DashboardResumoDTO buscarResumo(Long idUsuario) {
        List<Dashboard> lancamento = dashboardRepository.findByUsuarioIdUsuario(idUsuario);

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
}