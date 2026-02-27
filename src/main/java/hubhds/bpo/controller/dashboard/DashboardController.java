package hubhds.bpo.controller.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.dto.dashboard.resumo.DashboardResumoDTO;
import hubhds.bpo.service.dashboard.DashboardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    //Cria lançamento de receita
    @PostMapping("/{idUsuario}")
    public ResponseEntity<DashboardResponse> criar(
            @PathVariable Long idUsuario,
            @RequestBody @Valid DashboardRequest dashboardRequest
            ){
        DashboardResponse dashboardResponse = dashboardService.salvar(idUsuario, dashboardRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(dashboardResponse);
    }

    //Como o próprio nome diz gráfico de pizza na pág principal
    @GetMapping("/grafico-pizza/{idUsuario}")
    public ResponseEntity<List<Object[]>> buscarDadosPizza(@PathVariable Long idUsuario) {
        List<Object[]> dados = dashboardService.buscarDadosGraficoPizza(idUsuario);
        return ResponseEntity.ok(dados);
    }

    // Esse endpoint vai alimentar os Cards de Entradas/Saídas
    @GetMapping("/total/{idUsuario}")
    public ResponseEntity<List<Object[]>> buscarResumoTotal(@PathVariable Long idUsuario) {
        List<Object[]> resumo = dashboardService.buscarTotaisCards(idUsuario);
        return ResponseEntity.ok(resumo);
    }

    //Relatório de completo
    @GetMapping("/detalhado/{idUsuario}")
    public ResponseEntity<DashboardResumoDTO> buscarResumo(@PathVariable Long idUsuario) {
        DashboardResumoDTO resumo = dashboardService.buscarResumo(idUsuario);
        return  ResponseEntity.ok(resumo);
    }

    @PostMapping("/salvar/{idUsuario}")
    public ResponseEntity<?> salvarLancamento(
            @PathVariable Long idUsuario,
            @Valid @RequestBody DashboardRequest dashboardRequest) {
        try {
            DashboardResponse dashboardResponse = dashboardService.salvar(idUsuario, dashboardRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(dashboardResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}
