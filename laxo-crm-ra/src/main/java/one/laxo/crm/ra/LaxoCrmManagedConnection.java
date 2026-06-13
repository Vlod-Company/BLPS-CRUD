package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class LaxoCrmManagedConnection implements ManagedConnection {

    private final LaxoCrmManagedConnectionFactory managedConnectionFactory;
    private final LaxoCrmClientConfig config;
    private final List<ConnectionEventListener> listeners = new CopyOnWriteArrayList<>();
    private PrintWriter logWriter;
    private LaxoCrmConnectionImpl handle;

    public LaxoCrmManagedConnection(LaxoCrmManagedConnectionFactory managedConnectionFactory, LaxoCrmClientConfig config) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.config = config;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
        handle = new LaxoCrmConnectionImpl(this, new LaxoCrmClient(config));
        return handle;
    }

    @Override
    public void destroy() {
        cleanup();
    }

    @Override
    public void cleanup() {
        if (handle != null) {
            handle.invalidate();
            handle = null;
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof LaxoCrmConnectionImpl connectionHandle)) {
            throw new ResourceException("Unsupported connection handle: " + connection);
        }
        cleanup();
        connectionHandle.associate(this);
        handle = connectionHandle;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new ResourceException("XA transactions are not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new ResourceException("Local transactions are not supported");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() {
        return new LaxoCrmManagedConnectionMetaData();
    }

    void closeHandle(LaxoCrmConnectionImpl closedHandle) {
        if (handle == closedHandle) {
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            event.setConnectionHandle(closedHandle);
            for (ConnectionEventListener listener : listeners) {
                listener.connectionClosed(event);
            }
            handle = null;
        }
    }

    void connectionError(Exception exception, LaxoCrmConnectionImpl failedHandle) {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, exception);
        event.setConnectionHandle(failedHandle);
        for (ConnectionEventListener listener : listeners) {
            listener.connectionErrorOccurred(event);
        }
    }

    public LaxoCrmManagedConnectionFactory managedConnectionFactory() {
        return managedConnectionFactory;
    }
}
