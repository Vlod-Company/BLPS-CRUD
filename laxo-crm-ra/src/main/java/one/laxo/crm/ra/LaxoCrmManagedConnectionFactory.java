package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
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
import lombok.Getter;
import lombok.Setter;
import one.laxo.crm.api.LaxoCrmConnectionFactory;

@Getter
@Setter
public class LaxoCrmManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {

    private static final long serialVersionUID = 1L;

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
        return new LaxoCrmConnectionFactoryImpl(this, new StandaloneConnectionManager());
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

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, sid, crmName, connectTimeoutMillis, readTimeoutMillis, funnelId, orderStatusId, orderUserMentor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LaxoCrmManagedConnectionFactory other)) {
            return false;
        }
        return Objects.equals(baseUrl, other.baseUrl)
                && Objects.equals(sid, other.sid)
                && Objects.equals(crmName, other.crmName)
                && Objects.equals(connectTimeoutMillis, other.connectTimeoutMillis)
                && Objects.equals(readTimeoutMillis, other.readTimeoutMillis)
                && Objects.equals(funnelId, other.funnelId)
                && Objects.equals(orderStatusId, other.orderStatusId)
                && Objects.equals(orderUserMentor, other.orderUserMentor);
    }
}
