package hubhds.bpo.model.dashboard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MeioPagamento {

    CREDITO,
    DEBITO,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    CARTAO_DE_CREDITO,
    CARTAO_DE_DEBITO,
    PIX,
    BOLETO,
    DINHEIRO,
    TRANSFERENCIA,
    OUTROS;

    @JsonCreator
    public static MeioPagamento fromString(String value) {
        if (value == null || value.isBlank()) {
            return PIX;
        }

        String normalized = value
                .trim()
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("Ã", "A")
                .replace("Á", "A")
                .replace("À", "A")
                .replace("Â", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ô", "O")
                .replace("Õ", "O")
                .replace("Ú", "U")
                .replace("Ç", "C");

        return switch (normalized) {
            case "CREDITO", "CARTAO_CREDITO", "CARTAO_DE_CREDITO", "CREDIT_CARD" -> CREDITO;
            case "DEBITO", "CARTAO_DEBITO", "CARTAO_DE_DEBITO", "DEBIT_CARD" -> DEBITO;
            case "PIX" -> PIX;
            case "BOLETO" -> BOLETO;
            case "DINHEIRO" -> DINHEIRO;
            case "TRANSFERENCIA", "TRANSFERENCIA_BANCARIA", "TED", "DOC" -> TRANSFERENCIA;
            default -> OUTROS;
        };
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }

    public boolean isCartao() {
        return this == CREDITO
                || this == DEBITO
                || this == CARTAO_CREDITO
                || this == CARTAO_DEBITO
                || this == CARTAO_DE_CREDITO
                || this == CARTAO_DE_DEBITO;
    }

    public boolean isCredito() {
        return this == CREDITO
                || this == CARTAO_CREDITO
                || this == CARTAO_DE_CREDITO;
    }

    public boolean isDebito() {
        return this == DEBITO
                || this == CARTAO_DEBITO
                || this == CARTAO_DE_DEBITO;
    }
}