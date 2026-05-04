package hubhds.bpo.service.lancamento;

import hubhds.bpo.dto.lancamento.LancamentoRequest;
import hubhds.bpo.dto.lancamento.LancamentoResponse;
import hubhds.bpo.dto.lancamento.editar.LancamentoAtualizar;
import hubhds.bpo.model.categoria.Categoria;
import hubhds.bpo.model.lancamento.Lancamento;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.categoria.CategoriaRepository;
import hubhds.bpo.repository.lancamento.LancamentoRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public LancamentoService(
            LancamentoRepository lancamentoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public LancamentoResponse criarLancamento(Long idUsuario, LancamentoRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(request.idCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("A categoria não pertence a esse usuário");
        }

        String transactionId = request.transactionId();
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "trx_" + System.currentTimeMillis();
        }

        Lancamento lancamento = Lancamento.builder()
                .usuario(usuario)
                .categoria(categoria)
                .transactionId(transactionId)
                .idDashboard(request.idDashboard())
                .movimentacao(request.movimentacao())
                .valor(request.valor())
                .descricao(request.descricao())
                .dataTransacao(request.dataTransacao())
                .formaPagamento(request.formaPagamento())
                .tipoGasto(request.tipoGasto())
                .build();

        lancamento = lancamentoRepository.save(lancamento);

        return new LancamentoResponse(
                lancamento.getIdLancamento(),
                lancamento.getTransactionId(),
                categoria.getIdCategoria(),
                lancamento.getIdDashboard(),
                categoria.getNome(),
                lancamento.getMovimentacao(),
                lancamento.getValor(),
                lancamento.getDescricao(),
                lancamento.getDataTransacao(),
                lancamento.getFormaPagamento(),
                lancamento.getTipoGasto()
        );
    }

    @Transactional
    public LancamentoResponse atualizarLancamento(Long idUsuario, Long idLancamento, LancamentoAtualizar lancamentoAtualizar) {
        Lancamento lancamento = lancamentoRepository.findById(idLancamento)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        if (!lancamento.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Você não tem permissão para editar esse lançamento");
        }

        if (lancamentoAtualizar.movimentacao() !=null) {
            lancamento.setMovimentacao(lancamentoAtualizar.movimentacao());
        }

        if (lancamentoAtualizar.valor() !=null) {
            lancamento.setValor(lancamentoAtualizar.valor());
        }

        if (lancamentoAtualizar.descricao() !=null) {
            lancamento.setDescricao(lancamentoAtualizar.descricao());
        }

        if (lancamentoAtualizar.dataTransacao() !=null) {
            lancamento.setDataTransacao(lancamentoAtualizar.dataTransacao());
        }

        if (lancamentoAtualizar.formaPagamento() !=null) {
            lancamento.setFormaPagamento(lancamentoAtualizar.formaPagamento());
        }

        if (lancamentoAtualizar.tipoGasto() !=null) {
            lancamento.setTipoGasto(lancamentoAtualizar.tipoGasto());
        }

        Lancamento salvo = lancamentoRepository.save(lancamento);

        return new LancamentoResponse(
                salvo.getIdLancamento(),
                salvo.getTransactionId(),
                salvo.getCategoria().getIdCategoria(),
                salvo.getIdDashboard(),
                salvo.getCategoria().getNome(),
                salvo.getMovimentacao(),
                salvo.getValor(),
                salvo.getDescricao(),
                salvo.getDataTransacao(),
                salvo.getFormaPagamento(),
                salvo.getTipoGasto()
        );
    }

    @Transactional
    public void excluirLancamento(Long idUsuario, Long idLancamento) {
        Lancamento lancamento = lancamentoRepository.findById(idLancamento)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        if (!lancamento.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException("Você não tem permissão para excluir esse lançamento");
        }

        lancamentoRepository.delete(lancamento);
    }

    /* public List<LancamentoResponse> listarPorUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Lancamento> lancamentos = lancamentoRepository
                .findByUsuario_IdUsuarioOrderByDataTransacaoDesc(idUsuario);

        return lancamentos.stream()
                .map(l -> new LancamentoResponse(
                        l.getIdLancamento(),
                        l.getTransactionId(),
                        l.getCategoria().getIdCategoria(),
                        l.getIdDashboard(),
                        l.getCategoria().getNome(),
                        l.getMovimentacao(),
                        l.getValor(),
                        l.getDescricao(),
                        l.getDataTransacao(),
                        l.getFormaPagamento(),
                        l.getTipoGasto()
                ))
                .toList();
    } */

    public List<LancamentoResponse> listarPorUsuario(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Lancamento> lancamentos;

        if (perfilFinanceiro != null) {
            lancamentos = lancamentoRepository
                    .findByUsuarioIdUsuarioAndTipoGastoOrderByDataTransacaoDesc(idUsuario, perfilFinanceiro);
        } else {
            lancamentos = lancamentoRepository
                    .findByUsuarioIdUsuarioOrderByDataTransacaoDesc(idUsuario);
        }

        return lancamentos.stream()
                .map(l -> new LancamentoResponse(
                        l.getIdLancamento(),
                        l.getTransactionId(),
                        l.getCategoria().getIdCategoria(),
                        l.getIdDashboard(),
                        l.getCategoria().getNome(),
                        l.getMovimentacao(),
                        l.getValor(),
                        l.getDescricao(),
                        l.getDataTransacao(),
                        l.getFormaPagamento(),
                        l.getTipoGasto()
                ))
                .toList();
    }
}