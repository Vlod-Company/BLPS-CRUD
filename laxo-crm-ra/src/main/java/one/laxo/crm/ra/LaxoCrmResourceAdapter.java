package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import java.util.Objects;
import javax.transaction.xa.XAResource;

public class LaxoCrmResourceAdapter implements ResourceAdapter {

    @Override
    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {}

    @Override
    public void stop() {}

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        throw new UnsupportedOperationException("Inbound messaging is not supported by Laxo CRM resource adapter");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {}

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    @Override
    public int hashCode() {
        return Objects.hash(LaxoCrmResourceAdapter.class);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LaxoCrmResourceAdapter;
    }
}
