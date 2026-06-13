package one.laxo.crm.api;

public interface LaxoCrmConnection extends AutoCloseable {

    CrmPurchaseExportResult exportTicketPurchase(CrmPurchaseExportRequest request);

    @Override
    void close();
}
