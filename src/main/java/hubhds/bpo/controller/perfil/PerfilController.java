package hubhds.bpo.controller.perfil;

import hubhds.bpo.dto.usuario.perfil.PerfilDTO;
import hubhds.bpo.service.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @PutMapping("/{idUsuario}")
    public ResponseEntity<String> editarPerfil(@PathVariable Long idUsuario, @RequestBody PerfilDTO perfilDTO) {
        usuarioService.atualizarPerfil(idUsuario, perfilDTO);
        return ResponseEntity.ok("Perfil atualizado com sucesso");
    }
}
