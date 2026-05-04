package hubhds.bpo.model.dashboard;

public enum MeioPagamento {
    CREDITO,
    DEBITO;

    public static MeioPagamento fromMercadoPago(String paymentTypeId) {
        if ("credit_card".equalsIgnoreCase(paymentTypeId)) {
            return CREDITO;
        }

        if ("debit_card".equalsIgnoreCase(paymentTypeId)) {
            return DEBITO;
        }

        throw new IllegalArgumentException("Tipo de pagamento inválido: " + paymentTypeId);
    }
}
