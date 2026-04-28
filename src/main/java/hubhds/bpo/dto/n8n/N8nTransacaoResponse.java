package hubhds.bpo.dto.n8n;

public record N8nTransacaoResponse(
        boolean success,
        String transaction_id,
        String draft_id,
        String phone,
        String message
) {
}
