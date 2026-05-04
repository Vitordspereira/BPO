package hubhds.bpo.dto.checkout;

public record CheckoutAssinaturaRequest(
        String payerEmail,
        String cardTokenId,
        String paymentMethodId,
        String paymentTypeId,
        Integer installments,
        String issuerId
) {
}