package hubhds.bpo.service.cartao;

import hubhds.bpo.dto.cartao.CartaoDTO;
import hubhds.bpo.model.cartao.StatusCartao;
import hubhds.bpo.model.usuario.PerfilFinanceiro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;

import hubhds.bpo.repository.dashboard.DashboardRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.cartao.CartaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    private String formatarNumeroCartao(String numero) {
        if (numero == null) return null;

        String apenasNumeros = numero.replaceAll("\\D", "");
        return apenasNumeros.replaceAll("(\\d{4})(?=\\d)", "$1.");
    }

    @Transactional
    public Cartao criarCartao(CartaoDTO cartaoDTO) {

        if (cartaoDTO.nomeCartao() == null || cartaoDTO.nomeCartao().isBlank()) {
            throw new RuntimeException("Nome do cartão é obrigatório.");
        }

        if (cartaoDTO.idUsuario() == null) {
            throw new RuntimeException("O ID do usuário é obrigatório.");
        }

        if (cartaoDTO.perfilFinanceiro() == null) {
            throw new RuntimeException("O perfil financeiro é obrigatório.");
        }

        Usuario usuario = usuarioRepository.findById(cartaoDTO.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + cartaoDTO.idUsuario()));

        Cartao cartao = new Cartao();
        cartao.setNomeCartao(cartaoDTO.nomeCartao());
        cartao.setNumeroMascara(formatarNumeroCartao(cartaoDTO.numeroMascara()));
        cartao.setStatusCartao(cartaoDTO.statusCartao() != null ? cartaoDTO.statusCartao() : StatusCartao.ATIVO);
        cartao.setDiaFechamento(cartaoDTO.diaFechamento());
        cartao.setDiaVencimento(cartaoDTO.diaVencimento());
        cartao.setLimiteTotal(cartaoDTO.limiteTotal());
        cartao.setBandeira(cartaoDTO.bandeira());
        cartao.setCategoria(cartaoDTO.categoria());
        cartao.setSaldoEntrada(BigDecimal.ZERO);
        cartao.setSaldoSaida(BigDecimal.ZERO);
        cartao.setIcone(cartaoDTO.icone());
        cartao.setCor(cartaoDTO.cor());
        cartao.setUsuario(usuario);
        cartao.setPerfilFinanceiro(cartaoDTO.perfilFinanceiro());

        return cartaoRepository.save(cartao);
    }

    public List<Cartao> listarPorCartao(Long idUsuario, PerfilFinanceiro perfilFinanceiro) {
        return cartaoRepository.findByUsuario_IdUsuarioAndPerfilFinanceiro(idUsuario, perfilFinanceiro);
    }

    @Transactional
    public Cartao atualizarCartao(Long idCartao, CartaoDTO cartaoDTO) {
        Cartao cartaoExistente = cartaoRepository.findById(idCartao)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado com o ID: " + idCartao));

        if (cartaoDTO.nomeCartao() != null && !cartaoDTO.nomeCartao().isBlank()) {
            cartaoExistente.setNomeCartao(cartaoDTO.nomeCartao());
        }

        if (cartaoDTO.numeroMascara() != null && !cartaoDTO.numeroMascara().isBlank()) {
            cartaoExistente.setNumeroMascara(formatarNumeroCartao(cartaoDTO.numeroMascara()));
        }

        if (cartaoDTO.diaFechamento() != null) {
            cartaoExistente.setDiaFechamento(cartaoDTO.diaFechamento());
        }

        if (cartaoDTO.diaVencimento() != null) {
            cartaoExistente.setDiaVencimento(cartaoDTO.diaVencimento());
        }

        if (cartaoDTO.limiteTotal() != null) {
            cartaoExistente.setLimiteTotal(cartaoDTO.limiteTotal());
        }

        if (cartaoDTO.statusCartao() != null) {
            cartaoExistente.setStatusCartao(cartaoDTO.statusCartao());
        }

        if (cartaoDTO.bandeira() != null) {
            cartaoExistente.setBandeira(cartaoDTO.bandeira());
        }

        if (cartaoDTO.categoria() != null) {
            cartaoExistente.setCategoria(cartaoDTO.categoria());
        }

        if (cartaoDTO.icone() != null) {
            cartaoExistente.setIcone(cartaoDTO.icone());
        }

        if (cartaoDTO.cor() != null) {
            cartaoExistente.setCor(cartaoDTO.cor());
        }

        if (cartaoDTO.perfilFinanceiro() != null) {
            cartaoExistente.setPerfilFinanceiro(cartaoDTO.perfilFinanceiro());
        }

        if (cartaoDTO.idUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(cartaoDTO.idUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + cartaoDTO.idUsuario()));
            cartaoExistente.setUsuario(usuario);
        }

        return cartaoRepository.save(cartaoExistente);
    }

    public void excluirCartao(Long idCartao) {
        if (!cartaoRepository.existsById(idCartao)) {
            throw new RuntimeException("Não é possível excluir: Cartão não encontrado com o ID: " + idCartao);
        }

        dashboardRepository.deleteByCartao_IdCartao(idCartao);
        cartaoRepository.deleteById(idCartao);
    }

    @Transactional
    public void alternarStatusCartao(Long idCartao) {
        Cartao cartao = cartaoRepository.findById(idCartao)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

        cartao.setStatusCartao(
                cartao.getStatusCartao() == StatusCartao.ATIVO
                        ? StatusCartao.BLOQUEADO
                        : StatusCartao.ATIVO
        );

        cartaoRepository.save(cartao);
    }
}