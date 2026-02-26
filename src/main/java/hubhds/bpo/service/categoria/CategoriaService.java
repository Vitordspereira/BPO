package hubhds.bpo.service.categoria;

import hubhds.bpo.dto.categoria.CategoriaRequest;
import hubhds.bpo.dto.categoria.CategoriaResponse;
import hubhds.bpo.model.cadastro.Cadastro;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.repository.cadastro.CadastroRepository;
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
    private CadastroRepository cadastroRepository;

    public CategoriaResponse salvar (Long idCadastro,CategoriaRequest categoriaRequest) {

        Cadastro cadastro = cadastroRepository.findById(idCadastro)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = new Categoria();

        categoria.setCadastro(cadastro);
        categoria.setNome(categoriaRequest.nome());
        categoria.setTipo(categoriaRequest.tipo());
        categoria.setIcone(categoriaRequest.icone());
        categoria.setCor(categoriaRequest.cor());

        categoriaRepository.save(categoria);

        return new CategoriaResponse(categoria);
    }

    public List<CategoriaResponse> listarPorUsuario(@PathVariable Long idCadastro) {

        return categoriaRepository.findByCadastro_IdCadastro(idCadastro)
                .stream()
                .map(CategoriaResponse::new)
                .collect(Collectors.toList());
    }

    public CategoriaResponse atualizar(Long idCadastro, Long idCategoria, CategoriaRequest dto) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        if (!categoria.getCadastro().getIdCadastro().equals(idCadastro)) {
            throw new RuntimeException("Permissão negada: você não é o dono desta categoria!");
        }

        categoria.setNome(dto.nome());
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());

        categoriaRepository.save(categoria);
        return new CategoriaResponse(categoria);
    }

    public void deletar(Long idCategoria, Long idCadastro) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));
        if (!categoria.getCadastro().getIdCadastro().equals(idCadastro)) {
            throw new RuntimeException("Não tem permissão para excluir essa categoria");
        }

        categoriaRepository.delete(categoria);
    }
}
