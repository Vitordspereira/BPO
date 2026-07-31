package hubhds.bpo.controller.n8n;

import hubhds.bpo.dto.categorian8n.CategoriaN8nRequest;
import hubhds.bpo.dto.categorian8n.CategoriaN8nResponse;
import hubhds.bpo.dto.n8n.N8nTransacaoRequest;
import hubhds.bpo.dto.n8n.N8nTransacaoResponse;
import hubhds.bpo.dto.n8n.editar.N8nAtualizarRequest;
import hubhds.bpo.dto.n8n.editar.N8nAtualizarResponse;
import hubhds.bpo.model.categorian8n.CategoriaN8n;
import hubhds.bpo.model.n8n.N8n;
import hubhds.bpo.repository.categorian8n.CategoriaN8nRepository;
import hubhds.bpo.repository.n8n.TransacaoN8nRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/integracao/n8n")
public class N8nController {

    private final TransacaoN8nRepository transacaoN8nRepository;
    private final CategoriaN8nRepository categoriaN8nRepository;

    public N8nController(
            TransacaoN8nRepository transacaoN8nRepository,
            CategoriaN8nRepository categoriaN8nRepository
    ) {
        this.transacaoN8nRepository = transacaoN8nRepository;
        this.categoriaN8nRepository = categoriaN8nRepository;
    }

    @PostMapping("/{telefone}")
    public ResponseEntity<?> criarCategoria(
            @PathVariable String telefone,
            @RequestBody CategoriaN8nRequest categoriaN8nRequest
    ) {
        String telefoneTratado = trimToNull(telefone);

        if (telefoneTratado == null) {
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
        String slug = gerarSlugCategoria(nome);

        String perfilFinanceiro = normalizarPerfilFinanceiroCategoria(
                categoriaN8nRequest.perfilFinanceiro()
        );

        if (perfilFinanceiro == null) {
            perfilFinanceiro = "PESSOAL";
        }

        boolean categoriaJaExiste = categoriaN8nRepository
                .findByTelefoneAndSlugAndPerfilFinanceiroIgnoreCase(
                        telefoneTratado,
                        slug,
                        perfilFinanceiro
                )
                .isPresent();

        if (categoriaJaExiste) {
            return ResponseEntity.badRequest().body(
                    "Categoria já cadastrada para este telefone e perfil financeiro."
            );
        }

        CategoriaN8n categoriaN8n = CategoriaN8n.builder()
                .telefone(telefoneTratado)
                .nome(nome)
                .slug(slug)
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
                .perfilFinanceiro(perfilFinanceiro)
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
            categoria = "Automática";
        }

        /*
         * Regra nova:
         * Tanto DESPESA quanto RECEITA precisam de tipo_gasto.
         * Isso evita receita de EMPRESA aparecer também em PESSOAL.
         */
        if (tipoGasto == null) {
            erros.append("tipo_gasto deve ser EMPRESA ou PESSOAL; ");
        }

        /*
         * Forma de pagamento só é obrigatória para DESPESA.
         * Se vier vazia em DESPESA, assume PIX.
         * Se for RECEITA, não usa forma de pagamento.
         */
        if ("DESPESA".equals(movimentacao)) {
            if (formaPagamento == null) {
                formaPagamento = "PIX";
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

        CategoriaN8n categoriaEncontrada = buscarOuCriarCategoriaN8n(
                telefone,
                categoria,
                movimentacao,
                tipoGasto
        );

        String transactionId = "trx_" + UUID.randomUUID();

        N8n transacao = new N8n();
        transacao.setTransactionId(transactionId);
        transacao.setDraftId(draftId);
        transacao.setTelefone(telefone);
        transacao.setValor(valor);
        transacao.setDataTransacao(dataTransacao);
        transacao.setDescricao(descricao);
        transacao.setCategoria(categoriaEncontrada.getNome());
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

        if (n8nAtualizarRequest.movimentacao() != null && !n8nAtualizarRequest.movimentacao().isBlank()) {
            String movimentacao = normalizarMovimentacao(n8nAtualizarRequest.movimentacao());

            if (movimentacao == null) {
                return ResponseEntity.badRequest().body("movimentação deve ser DESPESA ou RECEITA.");
            }

            transacao.setMovimentacao(movimentacao);

            if ("RECEITA".equals(movimentacao)) {
                transacao.setFormaPagamento(null);
            }
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

            if (!"RECEITA".equalsIgnoreCase(transacao.getMovimentacao())) {
                transacao.setFormaPagamento(formaPagamento);
            }
        }

        if (
                transacao.getMovimentacao() != null
                        && transacao.getTipoGasto() == null
                        && (
                        "DESPESA".equalsIgnoreCase(transacao.getMovimentacao())
                                || "RECEITA".equalsIgnoreCase(transacao.getMovimentacao())
                )
        ) {
            return ResponseEntity.badRequest().body("tipo_gasto deve ser EMPRESA ou PESSOAL.");
        }

        if (n8nAtualizarRequest.categoria() != null && !n8nAtualizarRequest.categoria().isBlank()) {
            CategoriaN8n categoriaEncontrada = buscarOuCriarCategoriaN8n(
                    transacao.getTelefone(),
                    n8nAtualizarRequest.categoria().trim(),
                    transacao.getMovimentacao(),
                    transacao.getTipoGasto()
            );

            transacao.setCategoria(categoriaEncontrada.getNome());
        } else if (transacao.getCategoria() != null && !transacao.getCategoria().isBlank()) {
            CategoriaN8n categoriaEncontrada = buscarOuCriarCategoriaN8n(
                    transacao.getTelefone(),
                    transacao.getCategoria(),
                    transacao.getMovimentacao(),
                    transacao.getTipoGasto()
            );

            transacao.setCategoria(categoriaEncontrada.getNome());
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

    private CategoriaN8n buscarOuCriarCategoriaN8n(
            String telefone,
            String nomeCategoria,
            String movimentacao,
            String tipoGasto
    ) {
        String telefoneTratado = trimToNull(telefone);
        String nomeTratadoTemp = trimToNull(nomeCategoria);

        if (nomeTratadoTemp == null) {
            nomeTratadoTemp = "Automática";
        }

        final String nomeTratado = nomeTratadoTemp;
        final String slug = gerarSlugCategoria(nomeTratado);
        final String perfilFinanceiro = definirPerfilFinanceiroCategoria(tipoGasto);
        final String movimentacaoFinal = movimentacao;
        final String telefoneFinal = telefoneTratado;

        return categoriaN8nRepository
                .findByTelefoneAndSlugAndPerfilFinanceiroIgnoreCase(
                        telefoneFinal,
                        slug,
                        perfilFinanceiro
                )
                .orElseGet(() -> categoriaN8nRepository.save(
                        CategoriaN8n.builder()
                                .telefone(telefoneFinal)
                                .nome(nomeTratado)
                                .slug(slug)
                                .tipo(movimentacaoFinal)
                                .icone(null)
                                .cor(null)
                                .perfilFinanceiro(perfilFinanceiro)
                                .build()
                ));
    }

    private String definirPerfilFinanceiroCategoria(String tipoGasto) {
        String tipoTratado = normalizarTipoGasto(tipoGasto);

        if (tipoTratado == null) {
            throw new RuntimeException("tipo_gasto é obrigatório para definir se é PESSOAL ou EMPRESA.");
        }

        return tipoTratado;
    }

    private String normalizarPerfilFinanceiroCategoria(String perfilFinanceiro) {
        String valor = normalizarBase(perfilFinanceiro);

        if (valor == null) {
            return null;
        }

        return switch (valor) {
            case "EMPRESA" -> "EMPRESA";
            case "PESSOAL" -> "PESSOAL";
            default -> null;
        };
    }

    private String gerarSlugCategoria(String nome) {
        if (nome == null) return null;

        String texto = nome.trim();

        if (texto.isEmpty()) return null;

        texto = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return texto.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
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
            case "BOLETO" -> "BOLETO";
            case "TRANSFERENCIA", "TRANSFERENCIA_BANCARIA", "TED", "DOC" -> "TRANSFERENCIA";

            case "CREDITO",
                 "CARTAO_CREDITO",
                 "CARTAO_DE_CREDITO",
                 "CREDIT",
                 "CREDIT_CARD" -> "CARTAO_CREDITO";

            case "DEBITO",
                 "CARTAO_DEBITO",
                 "CARTAO_DE_DEBITO",
                 "DEBIT",
                 "DEBIT_CARD" -> "CARTAO_DEBITO";

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