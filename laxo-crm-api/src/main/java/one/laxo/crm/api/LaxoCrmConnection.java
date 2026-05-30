package one.laxo.crm.api;

public interface LaxoCrmConnection extends AutoCloseable {

    CrmPurchaseExportResult exportTicketPurchase(CrmPurchaseExportRequest request);

    CrmContactResult createOrUpdateContact(CrmContactRequest request);

    CrmDealResult createDeal(CrmDealRequest request);

    @Override
    void close();
}
