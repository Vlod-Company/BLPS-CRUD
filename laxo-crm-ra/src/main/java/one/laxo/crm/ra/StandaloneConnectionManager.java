package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

class StandaloneConnectionManager implements ConnectionManager {

    private static final long serialVersionUID = 1L;

    @Override
    public Object allocateConnection(ManagedConnectionFactory factory, ConnectionRequestInfo info) throws ResourceException {
        ManagedConnection managedConnection = factory.createManagedConnection(new Subject(), info);
        return managedConnection.getConnection(new Subject(), info);
    }
}
