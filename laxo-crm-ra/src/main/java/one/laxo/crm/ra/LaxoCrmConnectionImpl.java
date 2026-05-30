package one.laxo.crm.ra;

import one.laxo.crm.api.CrmContactRequest;
import one.laxo.crm.api.CrmContactResult;
import one.laxo.crm.api.CrmDealRequest;
import one.laxo.crm.api.CrmDealResult;
import one.laxo.crm.api.CrmPurchaseExportRequest;
import one.laxo.crm.api.CrmPurchaseExportResult;
import one.laxo.crm.api.LaxoCrmConnection;

public class LaxoCrmConnectionImpl implements LaxoCrmConnection {

    private LaxoCrmManagedConnection managedConnection;
    private LaxoCrmClient client;
    private boolean closed;

    LaxoCrmConnectionImpl(LaxoCrmManagedConnection managedConnection, LaxoCrmClient client) {
        this.managedConnection = managedConnection;
        this.client = client;
    }

    @Override
    public CrmPurchaseExportResult exportTicketPurchase(CrmPurchaseExportRequest request) {
        return execute(() -> client.exportTicketPurchase(request));
    }

    @Override
    public CrmContactResult createOrUpdateContact(CrmContactRequest request) {
        return execute(() -> client.createOrUpdateContact(request));
    }

    @Override
    public CrmDealResult createDeal(CrmDealRequest request) {
        return execute(() -> client.createDeal(request));
    }

    @Override
    public void close() {
        if (!closed && managedConnection != null) {
            closed = true;
            managedConnection.closeHandle(this);
        }
    }

    void associate(LaxoCrmManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
        this.closed = false;
    }

    void invalidate() {
        this.closed = true;
        this.managedConnection = null;
        this.client = null;
    }

    private <T> T execute(CrmCall<T> call) {
        ensureOpen();
        try {
            return call.execute();
        } catch (RuntimeException e) {
            managedConnection.connectionError(e, this);
            throw e;
        }
    }

    private void ensureOpen() {
        if (closed || managedConnection == null || client == null) {
            throw new LaxoCrmResourceAccessException("Laxo CRM connection is closed");
        }
    }

    @FunctionalInterface
    private interface CrmCall<T> {
        T execute();
    }
}
