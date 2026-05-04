package hubhds.bpo.service.AssinaturaMercadoPagoService;

import hubhds.bpo.dto.assinatura.Assinatura;
import hubhds.bpo.model.usuario.AmbienteUsuario;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class AssinaturaMercadoPagoService {

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;

    @Value("${mercadopago.base-url}")
    private String baseUrl;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.back-url}")
    private String backUrl;

    @Value("${mercadopago.subscription-reason}")
    private String reason;

    @Value("${mercadopago.currency-id}")
    private String currencyId;

    @Value("${mercadopago.subscription-monthly-amount}")
    private BigDecimal monthlyAmount;

    @Value("${mercadopago.subscription-yearly-amount}")
    private BigDecimal yearlyAmount;

    public AssinaturaMercadoPagoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);

        this.restTemplate = new RestTemplate(factory);
    }

    @Transactional
    public Assinatura criarAssinaturaAutorizadaComCartao(
            Usuario usuario,
            String tipoPlano,
            String cardTokenId
    ) {
        if (usuario == null) {
            throw new RuntimeException("Usuário é obrigatório.");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RuntimeException("E-mail do usuário é obrigatório.");
        }

        if (usuario.getTelefone() == null || usuario.getTelefone().isBlank()) {
            throw new RuntimeException("Telefone do usuário é obrigatório.");
        }

        if (cardTokenId == null || cardTokenId.isBlank()) {
            throw new RuntimeException("Token do cartão é obrigatório.");
        }

        PlanoConfig plano = resolverPlano(tipoPlano);

        String telefoneLimpo = limparTelefone(usuario.getTelefone());
        String emailTratado = usuario.getEmail().trim().toLowerCase();
        String externalReference = "telefone:" + telefoneLimpo;

        Map<String, Object> autoRecurring = new HashMap<>();
        autoRecurring.put("frequency", plano.frequency());
        autoRecurring.put("frequency_type", plano.frequencyType());
        autoRecurring.put("transaction_amount", plano.amount());
        autoRecurring.put("currency_id", currencyId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", montarReason(plano));
        payload.put("external_reference", externalReference);
        payload.put("payer_email", emailTratado);
        payload.put("card_token_id", cardTokenId);
        payload.put("back_url", backUrl);
        payload.put("auto_recurring", autoRecurring);
        payload.put("status", "authorized");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            System.out.println("========== MERCADO PAGO / CHECKOUT PROPRIO ==========");
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + emailTratado);
            System.out.println("MP EXTERNAL_REF     = " + externalReference);
            System.out.println("MP BACK_URL         = " + backUrl);
            System.out.println("MP AUTO_RECURRING   = " + autoRecurring);
            System.out.println("MP PAYLOAD COMPLETO = " + payload);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/preapproval",
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Resposta vazia do Mercado Pago.");
            }

            System.out.println("MP RESPONSE STATUS = " + response.getStatusCode());
            System.out.println("MP RESPONSE BODY   = " + body);

            String preapprovalId = valor(body.get("id"));
            String initPoint = valor(body.get("init_point"));
            String sandboxInitPoint = valor(body.get("sandbox_init_point"));
            String status = valor(body.get("status"));

            if ((preapprovalId == null || preapprovalId.isBlank()) && initPoint != null) {
                preapprovalId = extrairPreapprovalIdDoInitPoint(initPoint);
            }

            if (preapprovalId == null || preapprovalId.isBlank()) {
                throw new RuntimeException("Mercado Pago não retornou preapprovalId.");
            }

            usuario.setTelefone(telefoneLimpo);
            usuario.setEmail(emailTratado);
            usuario.setMpPreapprovalId(preapprovalId);
            usuario.setMpExternalReference(externalReference);
            usuario.setMpStatus(status);
            usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());
            usuario.setTipoPlano(plano.tipoPlano());
            usuario.setValorPlano(plano.amount());
            usuario.setPeriodicidadePlano(plano.periodicidade());

            if ("authorized".equalsIgnoreCase(status)) {
                usuario.setAssinaturaAtiva(true);
                usuario.setDataInatividade(null);
            } else {
                usuario.setAssinaturaAtiva(false);

                if (usuario.getDataInatividade() == null) {
                    usuario.setDataInatividade(LocalDateTime.now());
                }
            }

            usuarioRepository.save(usuario);

            return new Assinatura(
                    preapprovalId,
                    initPoint,
                    sandboxInitPoint,
                    status,
                    plano.tipoPlano(),
                    plano.amount(),
                    plano.periodicidade()
            );

        } catch (HttpStatusCodeException e) {
            String bodyErro = e.getResponseBodyAsString();

            System.out.println("========== ERRO MERCADO PAGO / CHECKOUT PROPRIO ==========");
            System.out.println("MP STATUS           = " + e.getStatusCode());
            System.out.println("MP BODY             = " + bodyErro);
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + emailTratado);
            System.out.println("MP EXTERNAL_REF     = " + externalReference);
            System.out.println("MP BACK_URL         = " + backUrl);
            System.out.println("MP AUTO_RECURRING   = " + autoRecurring);
            System.out.println("MP PAYLOAD COMPLETO = " + payload);

            throw new RuntimeException("Erro ao criar assinatura autorizada no Mercado Pago: " + bodyErro, e);
        }
    }

    @Transactional
    public Assinatura assinatura(Long idUsuario) {
        return assinatura(idUsuario, "MENSAL", null);
    }

    @Transactional
    public Assinatura assinatura(Long idUsuario, String tipoPlano) {
        return assinatura(idUsuario, tipoPlano, null);
    }

    @Transactional
    public Assinatura assinatura(Long idUsuario, String tipoPlano, String payerEmailParam) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String payerEmail = resolverPayerEmail(usuario, payerEmailParam);

        validarUsuarioParaAssinatura(usuario, payerEmail);

        PlanoConfig plano = resolverPlano(tipoPlano);

        String telefoneLimpo = limparTelefone(usuario.getTelefone());
        String externalReference = "telefone:" + telefoneLimpo;

        Map<String, Object> autoRecurring = new HashMap<>();
        autoRecurring.put("frequency", plano.frequency());
        autoRecurring.put("frequency_type", plano.frequencyType());
        autoRecurring.put("transaction_amount", plano.amount());
        autoRecurring.put("currency_id", currencyId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", montarReason(plano));
        payload.put("external_reference", externalReference);
        payload.put("payer_email", payerEmail);
        payload.put("back_url", backUrl);
        payload.put("auto_recurring", autoRecurring);
        payload.put("status", "pending");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            System.out.println("========== MERCADO PAGO / PREAPPROVAL PENDENTE ==========");
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + payerEmail);
            System.out.println("MP EXTERNAL_REF     = " + externalReference);
            System.out.println("MP BACK_URL         = " + backUrl);
            System.out.println("MP AUTO_RECURRING   = " + autoRecurring);
            System.out.println("MP PAYLOAD COMPLETO = " + payload);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/preapproval",
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Resposta vazia do Mercado Pago.");
            }

            System.out.println("MP RESPONSE STATUS = " + response.getStatusCode());
            System.out.println("MP RESPONSE BODY   = " + body);

            String preapprovalId = valor(body.get("id"));
            String initPoint = valor(body.get("init_point"));
            String sandboxInitPoint = valor(body.get("sandbox_init_point"));
            String status = valor(body.get("status"));

            if ((preapprovalId == null || preapprovalId.isBlank()) && initPoint != null) {
                preapprovalId = extrairPreapprovalIdDoInitPoint(initPoint);
            }

            usuario.setTelefone(telefoneLimpo);
            usuario.setMpPreapprovalId(preapprovalId);
            usuario.setMpExternalReference(externalReference);
            usuario.setMpStatus(status);
            usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());
            usuario.setTipoPlano(plano.tipoPlano());
            usuario.setValorPlano(plano.amount());
            usuario.setPeriodicidadePlano(plano.periodicidade());

            usuarioRepository.save(usuario);

            return new Assinatura(
                    preapprovalId,
                    initPoint,
                    sandboxInitPoint,
                    status,
                    plano.tipoPlano(),
                    plano.amount(),
                    plano.periodicidade()
            );

        } catch (HttpStatusCodeException e) {
            String bodyErro = e.getResponseBodyAsString();

            System.out.println("========== ERRO MERCADO PAGO / PREAPPROVAL PENDENTE ==========");
            System.out.println("MP STATUS           = " + e.getStatusCode());
            System.out.println("MP BODY             = " + bodyErro);
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + payerEmail);
            System.out.println("MP EXTERNAL_REF     = " + externalReference);
            System.out.println("MP BACK_URL         = " + backUrl);
            System.out.println("MP AUTO_RECURRING   = " + autoRecurring);
            System.out.println("MP PAYLOAD COMPLETO = " + payload);

            throw new RuntimeException("Erro ao criar assinatura no Mercado Pago: " + bodyErro, e);
        }
    }

    // =========================================================
    // Consulta assinatura no Mercado Pago
    // =========================================================
    public Map<String, Object> consultarAssinatura(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.isBlank()) {
            throw new IllegalArgumentException("preapprovalId é obrigatório.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            System.out.println("========== MERCADO PAGO / CONSULTAR ASSINATURA ==========");
            System.out.println("MP URL         = " + baseUrl + "/preapproval/" + preapprovalId);
            System.out.println("MP PREAPPROVAL = " + preapprovalId);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/preapproval/" + preapprovalId,
                    HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Assinatura não encontrada no Mercado Pago.");
            }

            System.out.println("MP RESPONSE STATUS = " + response.getStatusCode());
            System.out.println("MP RESPONSE BODY   = " + response.getBody());

            return response.getBody();

        } catch (HttpStatusCodeException e) {
            String bodyErro = e.getResponseBodyAsString();

            System.out.println("========== ERRO MERCADO PAGO / CONSULTAR ASSINATURA ==========");
            System.out.println("MP STATUS      = " + e.getStatusCode());
            System.out.println("MP BODY        = " + bodyErro);
            System.out.println("MP URL         = " + baseUrl + "/preapproval/" + preapprovalId);
            System.out.println("MP PREAPPROVAL = " + preapprovalId);

            throw new RuntimeException("Erro ao consultar assinatura no Mercado Pago: " + bodyErro, e);
        }
    }

    // =========================================================
    // Sincroniza assinatura e ativa/bloqueia usuário
    // usado por webhook e rotinas manuais
    // =========================================================
    @Transactional
    public Map<String, Object> sincronizarAssinatura(String preapprovalId) {
        Map<String, Object> assinaturaMp = consultarAssinatura(preapprovalId);

        String status = valor(assinaturaMp.get("status")).toLowerCase();
        String externalReference = valor(assinaturaMp.get("external_reference"));

        if (externalReference == null || externalReference.isBlank()) {
            throw new RuntimeException("External reference não encontrada na assinatura.");
        }

        if (!externalReference.startsWith("telefone:")) {
            throw new RuntimeException("External reference inválida: " + externalReference);
        }

        String telefone = externalReference.replace("telefone:", "").trim();

        Usuario usuario = usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o telefone: " + telefone));

        usuario.setMpPreapprovalId(preapprovalId);
        usuario.setMpExternalReference(externalReference);
        usuario.setMpStatus(status);
        usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());

        switch (status) {
            case "authorized":
                usuario.setAssinaturaAtiva(true);
                usuario.setDataInatividade(null);
                break;

            case "pending":
            case "paused":
            case "cancelled":
            case "canceled":
                usuario.setAssinaturaAtiva(false);

                if (usuario.getDataInatividade() == null) {
                    usuario.setDataInatividade(LocalDateTime.now());
                }
                break;

            default:
                break;
        }

        usuarioRepository.save(usuario);

        Map<String, Object> retorno = new HashMap<>();
        retorno.put("preapprovalId", preapprovalId);
        retorno.put("telefone", telefone);
        retorno.put("status", status);
        retorno.put("assinaturaAtiva", usuario.getAssinaturaAtiva());

        return retorno;
    }

    // =========================================================
    // Legado: iniciar assinatura por telefone e gerar link
    // Pode remover depois que o checkout novo substituir tudo
    // =========================================================
    @Transactional
    public Assinatura iniciarAssinatura(String telefone, String plano, String payerEmail) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }

        String telefoneLimpo = limparTelefone(telefone);

        Usuario usuario = usuarioRepository.findByTelefone(telefoneLimpo)
                .orElseGet(() -> {
                    Usuario novoUsuario = Usuario.builder()
                            .telefone(telefoneLimpo)
                            .senha(null)
                            .temEmpresa(0)
                            .assinaturaAtiva(false)
                            .ambienteUsuario(AmbienteUsuario.PESSOAL)
                            .build();

                    return usuarioRepository.save(novoUsuario);
                });

        String planoFinal = (plano == null || plano.isBlank()) ? "MENSAL" : plano;

        return assinatura(usuario.getIdUsuario(), planoFinal, payerEmail);
    }

    private void validarUsuarioParaAssinatura(Usuario usuario, String payerEmail) {
        if (payerEmail == null || payerEmail.isBlank()) {
            throw new IllegalArgumentException("payerEmail não informado para criar assinatura.");
        }

        if (!payerEmail.contains("@")) {
            throw new IllegalArgumentException("payerEmail inválido para criar assinatura.");
        }

        if (usuario.getTelefone() == null || usuario.getTelefone().isBlank()) {
            throw new IllegalArgumentException("Usuário sem telefone para criar external_reference.");
        }
    }

    private String resolverPayerEmail(Usuario usuario, String payerEmailParam) {
        if (payerEmailParam != null && !payerEmailParam.isBlank()) {
            return payerEmailParam.trim().toLowerCase();
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Usuário sem e-mail para criar assinatura.");
        }

        return usuario.getEmail().trim().toLowerCase();
    }

    private PlanoConfig resolverPlano(String tipoPlano) {
        if (tipoPlano == null || tipoPlano.isBlank()) {
            return new PlanoConfig("MENSAL", monthlyAmount, 1, "months", "MENSAL");
        }

        String planoNormalizado = tipoPlano.trim().toUpperCase();

        return switch (planoNormalizado) {
            case "MENSAL" -> new PlanoConfig("MENSAL", monthlyAmount, 1, "months", "MENSAL");
            case "ANUAL" -> new PlanoConfig("ANUAL", yearlyAmount, 12, "months", "ANUAL");
            default -> throw new IllegalArgumentException("Plano inválido. Use MENSAL ou ANUAL.");
        };
    }

    private String montarReason(PlanoConfig plano) {
        return reason + " - " + plano.tipoPlano();
    }

    private String limparTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }

        return telefone.replaceAll("\\D", "");
    }

    private String extrairPreapprovalIdDoInitPoint(String initPoint) {
        if (initPoint == null || initPoint.isBlank()) {
            return null;
        }

        String parametro = "preapproval_id=";

        int inicio = initPoint.indexOf(parametro);

        if (inicio == -1) {
            return null;
        }

        inicio += parametro.length();

        int fim = initPoint.indexOf("&", inicio);

        if (fim == -1) {
            return initPoint.substring(inicio);
        }

        return initPoint.substring(inicio, fim);
    }

    private String valor(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    private record PlanoConfig(
            String tipoPlano,
            BigDecimal amount,
            Integer frequency,
            String frequencyType,
            String periodicidade
    ) {
    }

    @Transactional
    public Assinatura criarAssinaturaAutorizadaPreCadastro(
            String payerEmail,
            String tipoPlano,
            String cardTokenId,
            String externalReference
    ) {
        if (payerEmail == null || payerEmail.isBlank()) {
            throw new RuntimeException("E-mail do pagador é obrigatório.");
        }

        if (cardTokenId == null || cardTokenId.isBlank()) {
            throw new RuntimeException("Token do cartão é obrigatório.");
        }

        if (externalReference == null || externalReference.isBlank()) {
            throw new RuntimeException("External reference é obrigatória.");
        }

        PlanoConfig plano = resolverPlano(tipoPlano);

        String emailTratado = payerEmail.trim().toLowerCase();

        Map<String, Object> autoRecurring = new HashMap<>();
        autoRecurring.put("frequency", plano.frequency());
        autoRecurring.put("frequency_type", plano.frequencyType());
        autoRecurring.put("transaction_amount", plano.amount());
        autoRecurring.put("currency_id", currencyId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", montarReason(plano));
        payload.put("external_reference", externalReference);
        payload.put("payer_email", emailTratado);
        payload.put("card_token_id", cardTokenId);
        payload.put("back_url", backUrl);
        payload.put("auto_recurring", autoRecurring);
        payload.put("status", "authorized");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/preapproval",
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Resposta vazia do Mercado Pago.");
            }

            String preapprovalId = valor(body.get("id"));
            String initPoint = valor(body.get("init_point"));
            String sandboxInitPoint = valor(body.get("sandbox_init_point"));
            String status = valor(body.get("status"));

            if ((preapprovalId == null || preapprovalId.isBlank()) && initPoint != null) {
                preapprovalId = extrairPreapprovalIdDoInitPoint(initPoint);
            }

            if (preapprovalId == null || preapprovalId.isBlank()) {
                throw new RuntimeException("Mercado Pago não retornou preapprovalId.");
            }

            return new Assinatura(
                    preapprovalId,
                    initPoint,
                    sandboxInitPoint,
                    status,
                    plano.tipoPlano(),
                    plano.amount(),
                    plano.periodicidade()
            );

        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(
                    "Erro ao criar assinatura de pré-cadastro no Mercado Pago: " + e.getResponseBodyAsString(),
                    e
            );
        }
    }
}