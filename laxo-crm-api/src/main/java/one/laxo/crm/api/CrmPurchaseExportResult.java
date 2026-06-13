package one.laxo.crm.api;

import java.io.Serializable;

public record CrmPurchaseExportResult(
        boolean success,
        String contactId,
        String dealId,
        String message
) implements Serializable {
}
