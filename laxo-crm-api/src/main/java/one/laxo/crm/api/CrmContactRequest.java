package one.laxo.crm.api;

import java.io.Serializable;

public record CrmContactRequest(
        String fullName,
        Long externalUserId,
        String documentNumber,
        String source
) {
}
