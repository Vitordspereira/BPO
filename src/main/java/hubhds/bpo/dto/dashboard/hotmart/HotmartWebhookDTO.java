package hubhds.bpo.dto.dashboard.hotmart;

import java.math.BigDecimal;

public record HotmartWebhookDTO(
        String event,
        HotmartData hotmartData
) {
    public record HotmartData(
            Product product,
            Purchase purchase
    ){}
    public record Product(String name) {}
    public record Purchase(
            BigDecimal full_price_value,
            BigDecimal comission_value,
            String status
    ) {}
}
