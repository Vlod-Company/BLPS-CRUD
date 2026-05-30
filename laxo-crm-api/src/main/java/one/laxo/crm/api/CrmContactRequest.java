package one.laxo.crm.api;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CrmContactRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fullName;
    private Long externalUserId;
    private String documentNumber;
    private String source;
}
