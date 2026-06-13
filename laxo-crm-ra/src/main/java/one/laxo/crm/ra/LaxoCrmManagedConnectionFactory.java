package one.laxo.crm.ra;

import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.security.auth.Subject;

import lombok.Data;

@Data
public class LaxoCrmManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {

    private String baseUrl = "https://api-dev.laxo.one";
    private String sid;
    private String crmName;
    private Integer connectTimeoutMillis = 5000;
    private Integer readTimeoutMillis = 20000;
    private Integer funnelId = 1;
    private Integer orderStatusId = 3;
    private Integer orderUserMentor = 1;
    private transient PrintWriter logWriter;
    private transient ResourceAdapter resourceAdapter;

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) {
        return new LaxoCrmConnectionFactoryImpl(this, connectionManager);
    }

    @Override
    public Object createConnectionFactory() {
        throw new UnsupportedOperationException("Will always run in managed environment");
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
        return new LaxoCrmManagedConnection(this, new LaxoCrmClientConfig(
                baseUrl,
                sid,
                crmName,
                connectTimeoutMillis,
                readTimeoutMillis,
                funnelId,
                orderStatusId,
                orderUserMentor
        ));
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info) {
        for (Object candidate : connectionSet) {
            if (candidate instanceof LaxoCrmManagedConnection connection && equals(connection.getManagedConnectionFactory())) {
                return connection;
            }
        }
        return null;
    }
}
