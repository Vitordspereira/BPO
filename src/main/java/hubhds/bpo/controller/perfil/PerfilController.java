package hubhds.bpo.controller.perfil;

import hubhds.bpo.dto.usuario.perfil.AlterarAmbienteDTO;
import hubhds.bpo.dto.usuario.perfil.AmbientesDTO;
import hubhds.bpo.dto.usuario.perfil.PerfilDTO;
import hubhds.bpo.service.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<PerfilDTO> buscarPerfil(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.buscarPerfil(idUsuario));
    }

    @PutMapping("/editar/{idUsuario}")
    public ResponseEntity<String> editarPerfil(@PathVariable Long idUsuario, @RequestBody PerfilDTO perfilDTO) {
        usuarioService.atualizarPerfil(idUsuario, perfilDTO);
        return ResponseEntity.ok("Perfil atualizado com sucesso");
    }

    @GetMapping("/{idUsuario}/ambientes")
    public ResponseEntity<AmbientesDTO> listarAmbientes(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.listarAmbientes(idUsuario));
    }

    @PutMapping("/{idUsuario}/ambiente")
    public ResponseEntity<String>alterarAmbiente(
            @PathVariable Long idUsuario,
            @RequestBody AlterarAmbienteDTO alterarAmbienteDTO
            ) {
        try{
        usuarioService.alterarAmbiente(idUsuario, alterarAmbienteDTO);
        return ResponseEntity.ok("Ambiente alterado com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
