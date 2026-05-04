package hubhds.bpo.service.checkout;

import hubhds.bpo.dto.assinatura.Assinatura;
import hubhds.bpo.dto.checkout.CheckoutAssinaturaRequest;
import hubhds.bpo.dto.checkout.CheckoutAssinaturaResponse;
import hubhds.bpo.model.meioPagamentoCheckout.MeioPagamentoCheckout;
import hubhds.bpo.model.preCadastro.PreCadastro;
import hubhds.bpo.repository.preCadastro.PreCadastroRepository;
import hubhds.bpo.service.AssinaturaMercadoPagoService.AssinaturaMercadoPagoService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckoutAssinaturaService {

    private static final BigDecimal VALOR_ASSINATURA = new BigDecimal("29.90");
    private static final String PLANO_PADRAO = "MENSAL";
    private static final int MAX_PARCELAS_CREDITO = 12;

    private final PreCadastroRepository preCadastroAssinaturaRepository;
    private final AssinaturaMercadoPagoService assinaturaMercadoPagoService;

    public CheckoutAssinaturaService(
            PreCadastroRepository preCadastroAssinaturaRepository,
            AssinaturaMercadoPagoService assinaturaMercadoPagoService
    ) {
        this.preCadastroAssinaturaRepository = preCadastroAssinaturaRepository;
        this.assinaturaMercadoPagoService = assinaturaMercadoPagoService;
    }

    @Transactional
    public CheckoutAssinaturaResponse assinar(CheckoutAssinaturaRequest request) {
        validarRequest(request);

        String payerEmail = request.payerEmail().trim().toLowerCase();
        String plano = PLANO_PADRAO;

        MeioPagamentoCheckout meioPagamento = MeioPagamentoCheckout.fromMercadoPago(request.paymentTypeId());

        validarParcelas(meioPagamento, request.installments());

        String token = UUID.randomUUID().toString();
        String externalReference = "pre_cadastro:" + token;

        Assinatura assinatura = assinaturaMercadoPagoService.criarAssinaturaAutorizadaPreCadastro(
                payerEmail,
                plano,
                request.cardTokenId().trim(),
                externalReference
        );

        PreCadastro preCadastro = PreCadastro.builder()
                .token(token)
                .payerEmail(payerEmail)
                .plano(plano)
                .valorPlano(VALOR_ASSINATURA)
                .periocidadePlano(assinatura.periodicidade())
                .mpPreapprovalId(assinatura.preapprovalId())
                .mpExternalReference(externalReference)
                .mpStatus(assinatura.status())
                .usado(false)
                .expiracao(LocalDateTime.now().plusDays(2))
                .build();

        preCadastroAssinaturaRepository.save(preCadastro);

        return new CheckoutAssinaturaResponse(
                token,
                "/pre-cadastro?token=" + token,
                assinatura.preapprovalId(),
                assinatura.status(),
                plano,
                VALOR_ASSINATURA,
                assinatura.periodicidade()
        );
    }

    private void validarRequest(CheckoutAssinaturaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do checkout são obrigatórios.");
        }

        if (request.payerEmail() == null || request.payerEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail do pagador é obrigatório.");
        }

        if (!request.payerEmail().contains("@")) {
            throw new IllegalArgumentException("E-mail do pagador inválido.");
        }

        if (request.cardTokenId() == null || request.cardTokenId().isBlank()) {
            throw new IllegalArgumentException("Token do cartão é obrigatório.");
        }

        if (request.paymentMethodId() == null || request.paymentMethodId().isBlank()) {
            throw new IllegalArgumentException("Bandeira do cartão é obrigatória.");
        }

        if (request.paymentTypeId() == null || request.paymentTypeId().isBlank()) {
            throw new IllegalArgumentException("Tipo de pagamento é obrigatório.");
        }

        if (request.installments() == null) {
            throw new IllegalArgumentException("Quantidade de parcelas é obrigatória.");
        }
    }

    private void validarParcelas(MeioPagamentoCheckout meioPagamento, Integer installments) {
        if (installments == null) {
            throw new IllegalArgumentException("Informe a quantidade de parcelas.");
        }

        if (meioPagamento == MeioPagamentoCheckout.CREDITO) {
            if (installments < 1 || installments > MAX_PARCELAS_CREDITO) {
                throw new IllegalArgumentException("Pagamento no crédito permite de 1 até 12 parcelas.");
            }
        }

        if (meioPagamento == MeioPagamentoCheckout.DEBITO) {
            if (installments != 1) {
            }
        }
    }
}