package one.laxo.crm.api;

public record CrmDealResult(
        boolean success,
        String dealId,
        String message
) {
}
