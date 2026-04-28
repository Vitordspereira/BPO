package hubhds.bpo.service.AssinaturaMercadoPagoService;

import hubhds.bpo.dto.assinaturaMercadoPago.Assinatura;
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

    @Value("${mercadopago.test-buyer-email:}")
    private String testBuyerEmail; // ATUALIZADO: agora será realmente usado

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
    public Assinatura assinatura(Long idUsuario) {
        return assinatura(idUsuario, "MENSAL", null); // ATUALIZADO
    }

    @Transactional
    public Assinatura assinatura(Long idUsuario, String tipoPlano) {
        return assinatura(idUsuario, tipoPlano, null); // ATUALIZADO: preserva compatibilidade
    }

    @Transactional
    public Assinatura assinatura(Long idUsuario, String tipoPlano, String payerEmailParam) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // ATUALIZADO: agora resolve o payerEmail primeiro
        String payerEmail = resolverPayerEmail(usuario, payerEmailParam);

        // ATUALIZADO: valida usando o payerEmail final
        validarUsuarioParaAssinatura(usuario, payerEmail);

        PlanoConfig plano = resolverPlano(tipoPlano);
        String externalReference = "telefone:" + limparTelefone(usuario.getTelefone());

        Map<String, Object> autoRecurring = new HashMap<>();
        autoRecurring.put("frequency", plano.frequency());
        autoRecurring.put("frequency_type", plano.frequencyType());
        autoRecurring.put("transaction_amount", plano.amount());
        autoRecurring.put("currency_id", currencyId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", montarReason(plano));
        payload.put("external_reference", externalReference);
        payload.put("payer_email", payerEmail); // ATUALIZADO: não usa mais direto usuario.getEmail()
        payload.put("back_url", backUrl);
        payload.put("auto_recurring", autoRecurring);
        payload.put("status", "pending");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            System.out.println("========== MERCADO PAGO / PREAPPROVAL ==========");
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + payerEmail); // ATUALIZADO
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
                throw new RuntimeException("Resposta vazia do Mercado Pago");
            }

            System.out.println("MP RESPONSE STATUS = " + response.getStatusCode());
            System.out.println("MP RESPONSE BODY   = " + body);

            String preapprovalId = valor(body.get("id"));
            String initPoint = valor(body.get("init_point"));
            String sandboxInitPoint = valor(body.get("sandbox_init_point")); // ATUALIZADO
            String status = valor(body.get("status"));

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
                    sandboxInitPoint, // ATUALIZADO
                    status,
                    plano.tipoPlano(),
                    plano.amount(),
                    plano.periodicidade()
            );

        } catch (HttpStatusCodeException e) {
            String bodyErro = e.getResponseBodyAsString();

            System.out.println("========== ERRO MERCADO PAGO / PREAPPROVAL ==========");
            System.out.println("MP STATUS           = " + e.getStatusCode());
            System.out.println("MP BODY             = " + bodyErro);
            System.out.println("MP URL              = " + baseUrl + "/preapproval");
            System.out.println("MP PAYER_EMAIL      = " + payerEmail); // ATUALIZADO
            System.out.println("MP EXTERNAL_REF     = " + externalReference);
            System.out.println("MP BACK_URL         = " + backUrl);
            System.out.println("MP AUTO_RECURRING   = " + autoRecurring);
            System.out.println("MP PAYLOAD COMPLETO = " + payload);

            throw new RuntimeException("Erro ao criar assinatura no Mercado Pago: " + bodyErro, e);
        }
    }

    public Map<String, Object> consultarAssinatura(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.isBlank()) {
            throw new IllegalArgumentException("preapprovalId é obrigatório");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            System.out.println("========== MERCADO PAGO / CONSULTAR ASSINATURA ==========");
            System.out.println("MP URL          = " + baseUrl + "/preapproval/" + preapprovalId);
            System.out.println("MP PREAPPROVAL  = " + preapprovalId);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/preapproval/" + preapprovalId,
                    HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getBody() == null) {
                throw new RuntimeException("Assinatura não encontrada no Mercado Pago");
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

    //Teste do mercado pago iniciar
    @Transactional
    public Assinatura iniciarAssinatura(String telefone, String plano, String payerEmail) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório e deve ser o mesmo para iniciar assinatura Mercado Pago");
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

    //novo preapproval
    @Transactional
    public Map<String, Object> sincronizarAssinatura(String preapprovalId) {
        Map<String, Object> assinaturaMp = consultarAssinatura(preapprovalId);

        String status = valor(assinaturaMp.get("status")).toLowerCase();
        String externalReference = valor(assinaturaMp.get("external_reference"));

        if (externalReference == null || externalReference.isBlank()) {
            throw new RuntimeException("External reference não encontrada na assinatura");
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
                usuario.setAssinaturaAtiva(false);

                if (usuario.getDataInatividade() == null) {
                    usuario.setDataInatividade(LocalDateTime.now());
                }
                break;

            default:
                // só registra o status no banco
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

    // ATUALIZADO: agora valida o payerEmail final, não só o email do usuário
    private void validarUsuarioParaAssinatura(Usuario usuario, String payerEmail) {
        if (payerEmail == null || payerEmail.isBlank()) {
            throw new IllegalArgumentException("payerEmail não informado para criar assinatura");
        }

        if (!payerEmail.contains("@")) {
            throw new IllegalArgumentException("payerEmail inválido para criar assinatura");
        }

        if (usuario.getTelefone() == null || usuario.getTelefone().isBlank()) {
            throw new IllegalArgumentException("Usuário sem telefone para criar external_reference");
        }
    }

    // ATUALIZADO: decide a origem do payerEmail
    private String resolverPayerEmail(Usuario usuario, String payerEmailParam) {
        if (payerEmailParam != null && !payerEmailParam.isBlank()) {
            return payerEmailParam.trim().toLowerCase();
        }

        if (testBuyerEmail != null && !testBuyerEmail.isBlank()) {
            return testBuyerEmail.trim().toLowerCase();
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Usuário sem e-mail e sem payerEmail de teste");
        }

        return usuario.getEmail().trim().toLowerCase();
    }

    private PlanoConfig resolverPlano(String tipoPlano) {
        if (tipoPlano == null || tipoPlano.isBlank()) {
            return new PlanoConfig("MENSAL", monthlyAmount, 1, "months", "MENSAL");
        }

        String planoNormalizado = tipoPlano.trim().toUpperCase();

        switch (planoNormalizado) {
            case "MENSAL":
                return new PlanoConfig("MENSAL", monthlyAmount, 1, "months", "MENSAL");
            case "ANUAL":
                return new PlanoConfig("ANUAL", yearlyAmount, 12, "months", "ANUAL");
            default:
                throw new IllegalArgumentException("Plano inválido. Use MENSAL ou ANUAL.");
        }
    }

    private String montarReason(PlanoConfig plano) {
        return reason + " - " + plano.tipoPlano();
    }

    private String limparTelefone(String telefone) {
        return telefone.replaceAll("\\D", "");
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
}