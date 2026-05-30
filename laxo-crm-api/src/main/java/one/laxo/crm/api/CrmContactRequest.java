package one.laxo.crm.api;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrmContactRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fullName;
    private Long externalUserId;
    private String documentNumber;
    private String source;
}
