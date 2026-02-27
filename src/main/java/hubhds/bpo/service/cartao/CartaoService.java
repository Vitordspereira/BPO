package hubhds.bpo.service.cartao;

import hubhds.bpo.dto.cartao.CartaoDTO;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.model.cartao.Cartao;

import hubhds.bpo.repository.usuario.UsuarioRepository;
import hubhds.bpo.repository.cartao.CartaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    public Cartao criarCartao (CartaoDTO cartaoDTO) {

        Usuario usuario = usuarioRepository.findById(cartaoDTO.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + cartaoDTO.idUsuario()));

        Cartao cartao = new Cartao();
        cartao.setNomeCartao(cartaoDTO.nomeCartao());

        //aplicação de máscara
        cartao.setNumeroMascara(formatarNumeroCartao(cartaoDTO.numeroMascara()));

        cartao.setDiaFechamento(cartaoDTO.diaFechamento());
        cartao.setDiaVencimento(cartaoDTO.diaVencimento());
        cartao.setLimiteTotal(cartaoDTO.limiteTotal());

        try {
            cartao.setBandeira(cartaoDTO.bandeira());
            cartao.setCategoria(cartaoDTO.categoria());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Bandeira ou Categoria inválida");
        }

        cartao.setSaldoEntrada(BigDecimal.ZERO);
        cartao.setSaldoSaida(BigDecimal.ZERO);

        cartao.setUsuario(usuario);
        return cartaoRepository.save(cartao);
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

        return cartaoRepository.save(cartaoExistente);
    }

    public void excluirCartao(Long idCartao) {
        //verifica se o cartão existe no banco
        if (!cartaoRepository.existsById(idCartao)) {
            throw new RuntimeException("Não é possível excluir: Cartão não encontrado com o ID: " + idCartao);
        }

        cartaoRepository.deleteById(idCartao);
    }
}
