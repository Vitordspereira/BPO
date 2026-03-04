package hubhds.bpo.service.cartao;

import hubhds.bpo.dto.cartao.CartaoDTO;
import hubhds.bpo.model.cartao.StatusCartao;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;

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

    //aplicação de máscara 0000.0000.0000
    private String formatarNumeroCartao(String numero){
        if (numero == null) return null;
        //Remove tudo que não for número
        String apenasNumeros = numero.replaceAll("\\D","");
        //Aplica o ponto a cada 4 dígito
        return apenasNumeros.replaceAll("(\\d{4})(?=\\d)", "$1.");
    }

    @Transactional
    public Cartao criarCartao (CartaoDTO cartaoDTO) {

        Usuario usuario = usuarioRepository.findById(cartaoDTO.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + cartaoDTO.idUsuario()));

        Cartao cartao = new Cartao();
        cartao.setNomeCartao(cartaoDTO.nomeCartao());

        //aplicação de máscara
        cartao.setNumeroMascara(formatarNumeroCartao(cartaoDTO.numeroMascara()));


        cartao.setStatusCartao(cartaoDTO.statusCartao() !=null ? cartaoDTO.statusCartao() : StatusCartao.ATIVO);

        cartao.setDiaFechamento(cartaoDTO.diaFechamento());
        cartao.setDiaVencimento(cartaoDTO.diaVencimento());
        cartao.setLimiteTotal(cartaoDTO.limiteTotal());
        cartao.setBandeira(cartaoDTO.bandeira());
        cartao.setCategoria(cartaoDTO.categoria());
        cartao.setSaldoEntrada(BigDecimal.ZERO);
        cartao.setSaldoSaida(BigDecimal.ZERO);

        cartao.setUsuario(usuario);
        return cartaoRepository.save(cartao);
    }

    public List<Cartao> listarPorCartao(Long idUsuario) {
        return cartaoRepository.findByUsuario_IdUsuario(idUsuario);
    }

    public Cartao atualizarCartao(Long idCartao, CartaoDTO cartaoDTO){
        //Buscar cartão no banco
        Cartao cartaoExistente = cartaoRepository.findById(idCartao)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado com o ID; " + idCartao));

        //Atualizar informações
        cartaoExistente.setNomeCartao(cartaoDTO.nomeCartao());

        //aplicação de máscara
        cartaoExistente.setNumeroMascara(formatarNumeroCartao(cartaoDTO.numeroMascara()));

        cartaoExistente.setDiaFechamento(cartaoDTO.diaFechamento());
        cartaoExistente.setDiaVencimento(cartaoDTO.diaVencimento());
        cartaoExistente.setLimiteTotal(cartaoDTO.limiteTotal());

        if (cartaoDTO.statusCartao() != null) cartaoExistente.setStatusCartao(cartaoDTO.statusCartao());
        if (cartaoDTO.bandeira() != null) cartaoExistente.setBandeira(cartaoDTO.bandeira());
        if (cartaoDTO.categoria() != null) cartaoExistente.setCategoria(cartaoDTO.categoria());

        return cartaoRepository.save(cartaoExistente);
    }

    public void excluirCartao(Long idCartao) {
        //verifica se o cartão existe no banco
        if (!cartaoRepository.existsById(idCartao)) {
            throw new RuntimeException("Não é possível excluir: Cartão não encontrado com o ID: " + idCartao);
        }

        cartaoRepository.deleteById(idCartao);
    }

    @Transactional
    public void alternarStatusCartao(Long idCartao){
        Cartao cartao = cartaoRepository.findById(idCartao)
                .orElseThrow(() -> new RuntimeException("Cartão não bloqueado"));

        cartao.setStatusCartao(cartao.getStatusCartao() == StatusCartao.ATIVO ? StatusCartao.BLOQUEADO : StatusCartao.ATIVO);
        cartaoRepository.save(cartao);
    }
}
