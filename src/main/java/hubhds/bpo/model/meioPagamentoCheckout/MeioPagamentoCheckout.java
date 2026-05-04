package hubhds.bpo.model.meioPagamentoCheckout;

public enum MeioPagamentoCheckout {
    CREDITO,
    DEBITO;

    public static MeioPagamentoCheckout fromMercadoPago(String paymentTypeId) {
        if ("credit_card".equalsIgnoreCase(paymentTypeId)) {
            return CREDITO;
        }

        if ("debit_card".equalsIgnoreCase(paymentTypeId)) {
            return DEBITO;
        }

        throw new IllegalArgumentException("Tipo de pagamento inválido: " + paymentTypeId);
    }
}