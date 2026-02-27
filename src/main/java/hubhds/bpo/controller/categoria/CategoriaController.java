package hubhds.bpo.controller.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.service.categoria.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Valid
    @PostMapping("/{idUsuario}")
    public ResponseEntity<CategoriaResponse> criar(@PathVariable Long idUsuario, @RequestBody @Valid CategoriaRequest categoriaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.salvar(idUsuario, categoriaRequest));
    }

    @GetMapping("/listar/{idUsuario}")
    public ResponseEntity<List<CategoriaResponse>> listarPorUsuario(@PathVariable Long idUsuario) {
        List<CategoriaResponse> categorias = categoriaService.listarPorUsuario(idUsuario);
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/editar/{idUsuario}/{idCategoria}")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long idUsuario,
            @PathVariable Long idCategoria,
            @RequestBody @Valid CategoriaRequest categoriaRequest
    ) {
        return ResponseEntity.ok(categoriaService.atualizar(idUsuario, idCategoria, categoriaRequest));
    }

    @DeleteMapping("/excluir/{idUsuario}/{idCategoria}")
    public ResponseEntity<Void> excluir(@PathVariable Long idUsuario, @PathVariable Long idCategoria) {
        categoriaService.deletar(idUsuario, idCategoria);

        return ResponseEntity.noContent().build();
    }
}
