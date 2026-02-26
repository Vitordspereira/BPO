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
    @PostMapping("/{idCadastro}")
    public ResponseEntity<CategoriaResponse> criar(@PathVariable Long idCadastro, @RequestBody @Valid CategoriaRequest categoriaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.salvar(idCadastro, categoriaRequest));
    }

    @GetMapping("/listar/{idCadastro}")
    public ResponseEntity<List<CategoriaResponse>> listarPorUsuario(@PathVariable Long idCadastro) {
        List<CategoriaResponse> categorias = categoriaService.listarPorUsuario(idCadastro);
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/editar/{idCadastro}/{idCategoria}")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long idCadastro,
            @PathVariable Long idCategoria,
            @RequestBody @Valid CategoriaRequest categoriaRequest
    ) {
        return ResponseEntity.ok(categoriaService.atualizar(idCadastro, idCategoria, categoriaRequest));
    }

    @DeleteMapping("/excluir/{idCadastro}/{idCategoria}")
    public ResponseEntity<Void> excluir(@PathVariable Long idCadastro, @PathVariable Long idCategoria) {
        categoriaService.deletar(idCadastro, idCategoria);

        return ResponseEntity.noContent().build();
    }
}
