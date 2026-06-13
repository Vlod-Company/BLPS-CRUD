package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.Referenceable;
import jakarta.resource.spi.ConnectionManager;
import javax.naming.Reference;

import lombok.RequiredArgsConstructor;
import one.laxo.crm.api.LaxoCrmConnection;
import one.laxo.crm.api.LaxoCrmConnectionFactory;

@RequiredArgsConstructor
public class LaxoCrmConnectionFactoryImpl implements LaxoCrmConnectionFactory, Referenceable {
    private final LaxoCrmManagedConnectionFactory managedConnectionFactory;
    private final ConnectionManager connectionManager;
    private Reference reference;

    @Override
    public LaxoCrmConnection getConnection() {
        try {
            return (LaxoCrmConnection) connectionManager.allocateConnection(managedConnectionFactory, null);
        } catch (ResourceException e) {
            throw new LaxoCrmResourceAccessException("Could not allocate Laxo CRM JCA connection", e);
        }
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }
}
