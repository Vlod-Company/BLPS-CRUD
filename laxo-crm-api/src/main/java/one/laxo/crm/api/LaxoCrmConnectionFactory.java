package one.laxo.crm.api;

import java.io.Serializable;

public interface LaxoCrmConnectionFactory extends Serializable {

    LaxoCrmConnection getConnection();
}
