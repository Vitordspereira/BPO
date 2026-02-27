package hubhds.bpo.service.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public CategoriaResponse salvar (Long idUsuario,CategoriaRequest categoriaRequest) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = new Categoria();

        categoria.setUsuario(usuario);
        categoria.setNome(categoriaRequest.nome());
        categoria.setTipo(categoriaRequest.tipo());
        categoria.setIcone(categoriaRequest.icone());
        categoria.setCor(categoriaRequest.cor());

        categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }

    public List<CategoriaResponse> listarPorUsuario(@PathVariable Long idUsuario) {

        return categoriaRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(CategoriaResponse::new)
                .collect(Collectors.toList());
    }

    public CategoriaResponse atualizar(Long idUsuario, Long idCategoria, CategoriaRequest dto) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Permissão negada: você não é o dono desta categoria!");
        }

        categoria.setNome(dto.nome());
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());

        categoriaRepository.save(categoria);
        return new CategoriaResponse(categoria);
    }

    public void deletar(Long idCategoria, Long idUsuario) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Não tem permissão para excluir essa categoria");
        }

        categoriaRepository.delete(categoria);
    }
}
