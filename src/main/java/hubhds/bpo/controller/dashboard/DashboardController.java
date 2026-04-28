package hubhds.bpo.controller.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.dto.dashboard.resumo.DashboardResumoDTO;
import hubhds.bpo.model.categoria.Tipo;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.service.dashboard.DashboardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    //Cria lançamento de receita e despesa
    @PostMapping("/{idUsuario}")
    public ResponseEntity<?> criar(
            @PathVariable Long idUsuario,
            @RequestBody @Valid DashboardRequest dashboardRequest
            ){
        try {
            DashboardResponse dashboardResponse = dashboardService.salvar(idUsuario, dashboardRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(dashboardResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro",e.getMessage()));
        }
    }

    //Como o próprio nome diz gráfico de pizza na pág principal
    @GetMapping("/grafico-pizza/{idUsuario}")
    public ResponseEntity<List<Object[]>> buscarDadosPizza(@PathVariable Long idUsuario, @RequestParam PerfilFinanceiro perfilFinanceiro, @RequestParam Tipo tipo) {
        List<Object[]> dados = dashboardService.buscarDadosGraficoPizza(idUsuario, perfilFinanceiro, tipo);
        return ResponseEntity.ok(dados);
    }

    // Esse endpoint vai alimentar os Cards de Entradas/Saídas
    @GetMapping("/total/{idUsuario}")
    public ResponseEntity<List<Object[]>> buscarResumoTotal(@PathVariable Long idUsuario, @RequestParam PerfilFinanceiro perfilFinanceiro) {
        List<Object[]> resumo = dashboardService.buscarTotaisCards(idUsuario, perfilFinanceiro);
        return ResponseEntity.ok(resumo);
    }

    //Relatório de completo
    @GetMapping("/resumo/{idUsuario}")
    public ResponseEntity<DashboardResumoDTO> buscarResumo(@PathVariable Long idUsuario, @RequestParam PerfilFinanceiro perfilFinanceiro) {
        DashboardResumoDTO resumo = dashboardService.buscarResumo(idUsuario, perfilFinanceiro);
        return  ResponseEntity.ok(resumo);
    }

    //Edita uma transação
    @PutMapping("/{idUsuario}/{idDashboard}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long idUsuario,
            @PathVariable Long idDashboard,
            @RequestBody @Valid DashboardRequest dashboardRequest
    ) {
        try{
            DashboardResponse dashboardResponse = dashboardService.atualizar(idUsuario, idDashboard, dashboardRequest);
            return ResponseEntity.ok(dashboardResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    //Exclui uma transação
    @DeleteMapping("/{idUsuario}/{idDashboard}")
    public ResponseEntity<?> deletar(
            @PathVariable Long idUsuario,
            @PathVariable Long idDashboard
    ) {
        try {
            dashboardService.deletar(idUsuario, idDashboard);
            return ResponseEntity.ok(Map.of("mensagem", "Transação excluida com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((Map.of("erro", e.getMessage())));
        }
    }
}
