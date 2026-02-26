package hubhds.bpo.service.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.model.cadastro.Cadastro;
import hubhds.bpo.model.cartao.Cartao;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.dashboard.Dashboard;
import hubhds.bpo.model.dashboard.MeioPagamento;
import hubhds.bpo.repository.cadastro.CadastroRepository;
import hubhds.bpo.repository.cartao.CartaoRepository;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import hubhds.bpo.repository.dashboard.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private CadastroRepository cadastroRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    public DashboardResponse salvar(Long idCadastro, DashboardRequest dashboardRequest){
        Cadastro cadastro = cadastroRepository.findById(idCadastro)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(dashboardRequest.idCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Dashboard dashboard = new Dashboard();
        dashboard.setCadastro(cadastro);
        dashboard.setCategoria(categoria);
        dashboard.setDescricao(dashboardRequest.descricao());
        dashboard.setValor(dashboardRequest.valor());
        dashboard.setData(dashboardRequest.data());
        dashboard.setTipo(dashboardRequest.tipo());
        dashboard.setMeioPagamento(dashboardRequest.meioPagamento());

        // 4. Lógica para Cartão (Crédito ou Débito)
        if (isMeioPagamentoCartao(dashboardRequest.meioPagamento()) && dashboardRequest.idCartao() != null) {
            Cartao cartao = cartaoRepository.findById(dashboardRequest.idCartao())
                    .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

            // Validação de segurança: O cartão pertence ao usuário do idCadastro?
            if (!cartao.getCadastro().getIdCadastro().equals(idCadastro)) {
                throw new RuntimeException("Este cartão não pertence a este usuário!");
            }

            dashboard.setCartao(cartao);
        }

        // 5. Salvamos e retornamos o Response formatado
        dashboardRepository.save(dashboard);
        return new DashboardResponse(dashboard);
    }

    // Mwtodo auxiliar para deixar o código mais limpo
    private boolean isMeioPagamentoCartao(MeioPagamento meio) {
        return meio == MeioPagamento.CARTAO_CREDITO || meio == MeioPagamento.CARTAO_DEBITO;
    }

    public List<Object[]> buscarDadosGraficoPizza(Long idCadastro) {
        return dashboardRepository.somarDespesasPorCategoria(idCadastro);
    }

    public List<Object[]> buscarTotaisCards(Long idCadastro) {
        return dashboardRepository.buscarResumoFinanceiro(idCadastro);
    }
}
