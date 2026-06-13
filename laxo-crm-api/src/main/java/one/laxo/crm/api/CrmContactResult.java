package one.laxo.crm.api;

import java.io.Serializable;

public record CrmContactResult(
        boolean success,
        String contactId,
        String message
) {
}
