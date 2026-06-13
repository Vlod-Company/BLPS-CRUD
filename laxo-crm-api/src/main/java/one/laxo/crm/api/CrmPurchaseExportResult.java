package one.laxo.crm.api;

public record CrmPurchaseExportResult(
        boolean success,
        String contactId,
        String dealId,
        String message
) {
}
