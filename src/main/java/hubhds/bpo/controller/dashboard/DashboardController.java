package hubhds.bpo.controller.dashboard;

import hubhds.bpo.dto.dashboard.DashboardRequest;
import hubhds.bpo.dto.dashboard.DashboardResponse;
import hubhds.bpo.service.dashboard.DashboardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/{idCadastro}")
    public ResponseEntity<DashboardResponse> criar(
            @PathVariable Long idCadastro,
            @RequestBody @Valid DashboardRequest dashboardRequest
            ){
        DashboardResponse dashboardResponse = dashboardService.salvar(idCadastro, dashboardRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(dashboardResponse);
    }

    @GetMapping("/grafico-pizza/{idCadastro}")
    public ResponseEntity<List<Object[]>> buscarDadosPizza(@PathVariable Long idCadastro) {
        List<Object[]> dados = dashboardService.buscarDadosGraficoPizza(idCadastro);
        return ResponseEntity.ok(dados);
    }

    // Esse endpoint vai alimentar os Cards de Entradas/Saídas
    @GetMapping("/total/{idCadastro}")
    public ResponseEntity<List<Object[]>> buscarResumo(@PathVariable Long idCadastro) {
        List<Object[]> resumo = dashboardService.buscarTotaisCards(idCadastro);
        return ResponseEntity.ok(resumo);
    }


}
