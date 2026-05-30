package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

class LaxoCrmManagedConnectionMetaData implements ManagedConnectionMetaData {

    @Override
    public String getEISProductName() {
        return "Laxo CRM";
    }

    @Override
    public String getEISProductVersion() {
        return "template";
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        return null;
    }
}
