package hubhds.bpo.controller.cartao;

import hubhds.bpo.dto.cartao.CartaoDTO;
import hubhds.bpo.model.cartao.Cartao;
import hubhds.bpo.service.cartao.CartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartao")
public class CartaoController {

    @Autowired
    private CartaoService cartaoService;

    @PostMapping("/cadastrar")
    public ResponseEntity<Cartao> cadastrar(@RequestBody CartaoDTO cartaoDTO) {
        Cartao novoCartao = cartaoService.criarCartao(cartaoDTO);
        return ResponseEntity.ok(novoCartao);
    }

    @PutMapping("/atualizar/{idCartao}")
    public ResponseEntity<Cartao> atualizar(@PathVariable Long idCartao, @RequestBody CartaoDTO cartaoDTO) {
        Cartao cartaoAtualizado = cartaoService.atualizarCartao(idCartao, cartaoDTO);
        return ResponseEntity.ok(cartaoAtualizado);
    }

    @DeleteMapping("/excluir/{idCartao}")
    public ResponseEntity<Cartao> excluir(@PathVariable Long idCartao) {
        cartaoService.excluirCartao(idCartao);
        return ResponseEntity.noContent().build();
    }
}