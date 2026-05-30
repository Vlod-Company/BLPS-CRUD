package one.laxo.crm.api;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrmContactResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String contactId;
    private String message;
}
