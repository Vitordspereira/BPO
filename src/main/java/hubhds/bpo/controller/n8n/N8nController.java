package hubhds.bpo.controller.n8n;

import hubhds.bpo.dto.categorian8n.CategoriaN8nRequest;
import hubhds.bpo.dto.categorian8n.CategoriaN8nResponse;
import hubhds.bpo.dto.n8n.N8nTransacaoRequest;
import hubhds.bpo.dto.n8n.N8nTransacaoResponse;
import hubhds.bpo.dto.n8n.editar.N8nAtualizarRequest;
import hubhds.bpo.dto.n8n.editar.N8nAtualizarResponse;
import hubhds.bpo.model.categorian8n.CategoriaN8n;
import hubhds.bpo.model.n8n.N8n;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.categorian8n.CategoriaN8nRepository;
import hubhds.bpo.repository.n8n.TransacaoN8nRepository;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/integracao/n8n")
public class N8nController {

    private final TransacaoN8nRepository transacaoN8nRepository;
    private final CategoriaN8nRepository categoriaN8nRepository;
    private final UsuarioRepository usuarioRepository;

    public N8nController(
            TransacaoN8nRepository transacaoN8nRepository,
            CategoriaN8nRepository categoriaN8nRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.transacaoN8nRepository = transacaoN8nRepository;
        this.categoriaN8nRepository = categoriaN8nRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/{telefone}")
    public ResponseEntity<?> criarCategoria(
            @PathVariable String telefone,
            @RequestBody CategoriaN8nRequest categoriaN8nRequest
    ) {
        if (telefone == null || telefone.isBlank()) {
            return ResponseEntity.badRequest().body("telefone é obrigatório.");
        }

        if (categoriaN8nRequest.nome() == null || categoriaN8nRequest.nome().isBlank()) {
            return ResponseEntity.badRequest().body("nome da categoria é obrigatório.");
        }

        if (categoriaN8nRequest.tipo() == null || categoriaN8nRequest.tipo().isBlank()) {
            return ResponseEntity.badRequest().body("tipo da categoria é obrigatório.");
        }

        String nome = categoriaN8nRequest.nome().trim();
        String tipo = categoriaN8nRequest.tipo().trim().toUpperCase();

        boolean categoriaJaExiste = categoriaN8nRepository
                .findByTelefoneAndNomeIgnoreCase(telefone, nome)
                .isPresent();

        if (categoriaJaExiste) {
            return ResponseEntity.badRequest().body("Categoria já cadastrada para este telefone.");
        }

        CategoriaN8n categoriaN8n = CategoriaN8n.builder()
                .telefone(telefone.trim())
                .nome(nome)
                .tipo(tipo)
                .icone(
                        categoriaN8nRequest.icone() != null
                                ? categoriaN8nRequest.icone().trim()
                                : null
                )
                .cor(
                        categoriaN8nRequest.cor() != null
                                ? categoriaN8nRequest.cor().trim()
                                : null
                )
                .perfilFinanceiro(
                        categoriaN8nRequest.perfilFinanceiro() != null
                                ? categoriaN8nRequest.perfilFinanceiro().trim().toUpperCase()
                                : null
                )
                .build();

        CategoriaN8n salva = categoriaN8nRepository.save(categoriaN8n);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoriaN8nResponse(salva));
    }

    @PostMapping
    public ResponseEntity<N8nTransacaoResponse> receberTransacao(
            @RequestBody N8nTransacaoRequest n8nTransacaoRequest
    ) {
        String draftId = trimToNull(n8nTransacaoRequest.draftId());
        String telefone = trimToNull(n8nTransacaoRequest.telefone());
        BigDecimal valor = n8nTransacaoRequest.valor();
        LocalDate dataTransacao = n8nTransacaoRequest.dataTransacao();
        String descricao = trimToNull(n8nTransacaoRequest.descricao());
        String categoria = trimToNull(n8nTransacaoRequest.categoria());
        String movimentacao = normalizarMovimentacao(n8nTransacaoRequest.movimentacao());
        String tipoGasto = normalizarTipoGasto(n8nTransacaoRequest.tipoGasto());
        String formaPagamento = normalizarFormaPagamento(n8nTransacaoRequest.formaPagamento());
        String status = trimToNull(n8nTransacaoRequest.status());

        StringBuilder erros = new StringBuilder();

        if (draftId == null) {
            erros.append("draft_id é obrigatório; ");
        }

        if (telefone == null) {
            erros.append("telefone é obrigatório; ");
        }

        if (valor == null) {
            erros.append("valor é obrigatório; ");
        }

        if (dataTransacao == null) {
            erros.append("data_transacao é obrigatória; ");
        }

        if (descricao == null) {
            erros.append("descricao é obrigatória; ");
        }

        if (status == null) {
            erros.append("status é obrigatório; ");
        }

        if (movimentacao == null) {
            erros.append("movimentacao deve ser DESPESA ou RECEITA; ");
        }

        if (categoria == null) {
            categoria = "automatica";
        }

        if (formaPagamento == null) {
            formaPagamento = "PIX";
        }

        if ("DESPESA".equals(movimentacao)) {
            if (tipoGasto == null) {
                erros.append("tipo_gasto deve ser EMPRESA ou PESSOAL quando a movimentacao for DESPESA; ");
            }
        } else if ("RECEITA".equals(movimentacao)) {
            formaPagamento = null;
        }

        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new N8nTransacaoResponse(
                            false,
                            null,
                            draftId,
                            telefone,
                            "Erro na requisição: " + erros
                    )
            );
        }

        final String telefoneFinal = telefone;
        final String categoriaFinal = categoria;
        final String movimentacaoFinal = movimentacao;
        final String tipoGastoFinal = tipoGasto;

        categoriaN8nRepository.findByTelefoneAndNomeIgnoreCase(telefone, categoria)
                .orElseGet(() -> categoriaN8nRepository.save(
                        CategoriaN8n.builder()
                                .telefone(telefoneFinal)
                                .nome(categoriaFinal)
                                .tipo(movimentacaoFinal)
                                .icone(null)
                                .cor(null)
                                .perfilFinanceiro(tipoGastoFinal)
                                .build()
                ));

        String transactionId = "trx_" + UUID.randomUUID();

        N8n transacao = new N8n();
        transacao.setTransactionId(transactionId);
        transacao.setDraftId(draftId);
        transacao.setTelefone(telefone);
        transacao.setValor(valor);
        transacao.setDataTransacao(dataTransacao);
        transacao.setDescricao(descricao);
        transacao.setCategoria(categoria);
        transacao.setMovimentacao(movimentacao);
        transacao.setTipoGasto(tipoGasto);
        transacao.setFormaPagamento(formaPagamento);
        transacao.setStatus(status);

        transacaoN8nRepository.save(transacao);

        return ResponseEntity.ok(
                new N8nTransacaoResponse(
                        true,
                        transactionId,
                        draftId,
                        telefone,
                        "Transação confirmada com sucesso."
                )
        );
    }

    @GetMapping("/{telefone}")
    public ResponseEntity<List<N8n>> listarTransacoesPorTelefone(@PathVariable String telefone) {
        String telefoneTratado = trimToNull(telefone);

        if (telefoneTratado == null) {
            return ResponseEntity.badRequest().body(List.of());
        }

        List<N8n> transacoes = transacaoN8nRepository
                .findByTelefoneOrderByDataTransacaoDescTransactionIdDesc(telefoneTratado);

        return ResponseEntity.ok(transacoes);
    }

    @GetMapping("/ultimos-5-dias/{telefone}")
    public ResponseEntity<List<N8n>> listarTransacoesUltimos5Dias(@PathVariable String telefone) {
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(4);

        List<N8n> transacoes = transacaoN8nRepository
                .findByTelefoneAndDataTransacaoBetweenOrderByDataTransacaoDescTransactionIdDesc(
                        telefone,
                        dataInicio,
                        dataFim
                );

        return ResponseEntity.ok(transacoes);
    }

    @GetMapping("/historico-mes/{telefone}/{ano}/{mes}")
    public ResponseEntity<?> listarHistoricoMes(
            @PathVariable String telefone,
            @PathVariable int ano,
            @PathVariable int mes
    ) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().body("Mês inválido");
        }

        LocalDate inicioDoMes = LocalDate.of(ano, mes, 1);
        LocalDate fimDoMes = inicioDoMes.withDayOfMonth(inicioDoMes.lengthOfMonth());

        List<N8n> transacoes = transacaoN8nRepository
                .findByTelefoneAndDataTransacaoBetweenOrderByDataTransacaoDescTransactionIdDesc(
                        telefone,
                        inicioDoMes,
                        fimDoMes
                );

        return ResponseEntity.ok(transacoes);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<?> editarTransacao(
            @PathVariable String transactionId,
            @RequestBody N8nAtualizarRequest n8nAtualizarRequest
    ) {
        Optional<N8n> optionalTransacao = transacaoN8nRepository.findById(transactionId);

        if (optionalTransacao.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Transação não encontrada.");
        }

        N8n transacao = optionalTransacao.get();

        if (n8nAtualizarRequest.valor() != null) {
            transacao.setValor(n8nAtualizarRequest.valor());
        }

        if (n8nAtualizarRequest.descricao() != null && !n8nAtualizarRequest.descricao().isBlank()) {
            transacao.setDescricao(n8nAtualizarRequest.descricao().trim());
        }

        if (n8nAtualizarRequest.categoria() != null && !n8nAtualizarRequest.categoria().isBlank()) {
            transacao.setCategoria(n8nAtualizarRequest.categoria().trim());
        }

        if (n8nAtualizarRequest.movimentacao() != null && !n8nAtualizarRequest.movimentacao().isBlank()) {
            String movimentacao = normalizarMovimentacao(n8nAtualizarRequest.movimentacao());

            if (movimentacao == null) {
                return ResponseEntity.badRequest().body("movimentação deve ser DESPESA ou RECEITA.");
            }

            transacao.setMovimentacao(movimentacao);
        }

        if (n8nAtualizarRequest.tipoGasto() != null && !n8nAtualizarRequest.tipoGasto().isBlank()) {
            String tipoGasto = normalizarTipoGasto(n8nAtualizarRequest.tipoGasto());

            if (tipoGasto == null) {
                return ResponseEntity.badRequest().body("Tipo gasto inválido.");
            }

            transacao.setTipoGasto(tipoGasto);
        }

        if (n8nAtualizarRequest.formaPagamento() != null && !n8nAtualizarRequest.formaPagamento().isBlank()) {
            String formaPagamento = normalizarFormaPagamento(n8nAtualizarRequest.formaPagamento());

            if (formaPagamento == null) {
                return ResponseEntity.badRequest().body("forma_pagamento inválida.");
            }

            transacao.setFormaPagamento(formaPagamento);
        }

        if (n8nAtualizarRequest.status() != null && !n8nAtualizarRequest.status().isBlank()) {
            transacao.setStatus(n8nAtualizarRequest.status().trim());
        }

        N8n transacaoAtualizada = transacaoN8nRepository.save(transacao);

        N8nAtualizarResponse n8nAtualizarResponse = new N8nAtualizarResponse(
                transacaoAtualizada.getTelefone(),
                transacaoAtualizada.getValor(),
                transacaoAtualizada.getDescricao(),
                transacaoAtualizada.getCategoria(),
                transacaoAtualizada.getMovimentacao(),
                transacaoAtualizada.getTipoGasto(),
                transacaoAtualizada.getFormaPagamento(),
                null,
                transacaoAtualizada.getStatus()
        );

        return ResponseEntity.ok(n8nAtualizarResponse);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> excluirTransacao(@PathVariable String transactionId) {
        Optional<N8n> optionalTransacao = transacaoN8nRepository.findById(transactionId);

        if (optionalTransacao.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Transação não encontrada.");
        }

        N8n transacao = optionalTransacao.get();
        transacaoN8nRepository.delete(transacao);

        return ResponseEntity.ok("Transação " + transacao.getTransactionId() + " excluída com sucesso.");
    }

    private String trimToNull(String valor) {
        if (valor == null) return null;

        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private String normalizarMovimentacao(String valor) {
        String v = normalizarBase(valor);

        if (v == null) return null;

        return switch (v) {
            case "DESPESA" -> "DESPESA";
            case "RECEITA" -> "RECEITA";
            default -> null;
        };
    }

    private String normalizarTipoGasto(String valor) {
        String v = normalizarBase(valor);

        if (v == null) return null;

        return switch (v) {
            case "EMPRESA" -> "EMPRESA";
            case "PESSOAL" -> "PESSOAL";
            default -> null;
        };
    }

    private String normalizarFormaPagamento(String valor) {
        String v = normalizarBase(valor);

        if (v == null) return null;

        return switch (v) {
            case "PIX" -> "PIX";
            case "DINHEIRO" -> "DINHEIRO";
            case "CARTAO_CREDITO" -> "CARTAO_CREDITO";
            case "CARTAO_DEBITO" -> "CARTAO_DEBITO";
            case "TRANSFERENCIA" -> "TRANSFERENCIA";
            case "BOLETO" -> "BOLETO";
            default -> null;
        };
    }

    private String normalizarBase(String valor) {
        if (valor == null) return null;

        String texto = valor.trim();

        if (texto.isEmpty()) return null;

        texto = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return texto.toUpperCase().replace(" ", "_");
    }
}