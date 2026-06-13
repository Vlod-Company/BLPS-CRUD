package one.laxo.crm.ra;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import lombok.Data;

import java.util.Objects;
import javax.transaction.xa.XAResource;

@Data
public class LaxoCrmResourceAdapter implements ResourceAdapter {

    @Override
    public void start(BootstrapContext bootstrapContext) {}

    @Override
    public void stop() {}

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        throw new UnsupportedOperationException("Inbound messaging is not supported");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        throw new UnsupportedOperationException("Inbound messaging is not supported");
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        throw new ResourceException("XA not supported");
    }
}
