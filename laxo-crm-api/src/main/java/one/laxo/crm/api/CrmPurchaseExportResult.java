package one.laxo.crm.api;

import java.io.Serializable;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmPurchaseExportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String contactId;
    private String dealId;
    private String message;
}
