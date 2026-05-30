package one.laxo.crm.ra;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import one.laxo.crm.api.CrmPurchaseExportRequest;
import one.laxo.crm.api.CrmPurchaseExportResult;
import one.laxo.crm.api.LaxoCrmConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class LaxoCrmLiveIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "LIVE_CRM_ENABLED", matches = "true")
    void exportTicketPurchaseCreatesContactAndDealInLiveCrm() {
        LaxoCrmManagedConnectionFactory managedConnectionFactory = new LaxoCrmManagedConnectionFactory();
        managedConnectionFactory.setBaseUrl(env("LIVE_CRM_BASE_URL", "https://api-dev.laxo.one"));
        managedConnectionFactory.setSid(requiredEnv("LIVE_CRM_SID"));
        managedConnectionFactory.setCrmName(env("LIVE_CRM_NAME", "pool"));
        managedConnectionFactory.setConnectTimeoutMillis(intEnv("LIVE_CRM_CONNECT_TIMEOUT_MS", 5000));
        managedConnectionFactory.setReadTimeoutMillis(intEnv("LIVE_CRM_READ_TIMEOUT_MS", 20000));
        managedConnectionFactory.setFunnelId(intEnv("LIVE_CRM_FUNNEL_ID", 1));
        managedConnectionFactory.setOrderStatusId(intEnv("LIVE_CRM_ORDER_STATUS_ID", 3));
        managedConnectionFactory.setOrderUserMentor(intEnv("LIVE_CRM_ORDER_USER_MENTOR", 1));

        LaxoCrmConnectionFactory connectionFactory =
                (LaxoCrmConnectionFactory) managedConnectionFactory.createConnectionFactory();

        CrmPurchaseExportRequest request = purchaseRequest();
        try (var connection = connectionFactory.getConnection()) {
            CrmPurchaseExportResult result = connection.exportTicketPurchase(request);

            assertTrue(result.isSuccess(), result.getMessage());
            assertNotNull(result.getContactId(), "CRM contact id must be returned");
            assertFalse(result.getContactId().isBlank(), "CRM contact id must not be blank");
            assertNotNull(result.getDealId(), "CRM deal id must be returned");
            assertFalse(result.getDealId().isBlank(), "CRM deal id must not be blank");

            System.out.printf(
                    "Laxo CRM live export created contactId=%s, dealId=%s, orderId=%s%n",
                    result.getContactId(),
                    result.getDealId(),
                    request.getOrderId()
            );
        }
    }

    private static CrmPurchaseExportRequest purchaseRequest() {
        long uniqueId = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        CrmPurchaseExportRequest request = new CrmPurchaseExportRequest();
        request.setOrderId(uniqueId);
        request.setUserId(1000L + uniqueId % 100000);
        request.setCreatedAt(now);
        request.setTotalPrice(new BigDecimal("12345.67"));
        request.setCurrency("RUB");
        request.setOrderStatus("PAID");
        request.setPaymentMethod("INTERNAL");
        request.setExternalLink("live-jca-test-" + uniqueId);
        request.setTicketId(uniqueId + 1);
        request.setSeatNumber("12A");
        request.setSeatClass("ECONOMY");
        request.setHasBaggage(true);
        request.setPassengerName("BLPS JCA Test " + uniqueId);
        request.setPassengerPassport("4510 123456");
        request.setFlightId(777L);
        request.setFlightNumber("BLPS-" + uniqueId % 10000);
        request.setDepartureAirport("LED");
        request.setArrivalAirport("SVO");
        request.setDepartureTime(now.plusDays(3));
        request.setArrivalTime(now.plusDays(3).plusHours(2));
        request.setAircraftType("Airbus A320");
        request.setBasePrice(new BigDecimal("10000.00"));
        request.setAirlineName("BLPS Test Airlines");
        request.setAirlineIataCode("BT");
        request.setAirlineCountry("RU");
        request.setAirlineWebsiteUrl("https://example.com/blps-jca-test");
        return request;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable is required for live CRM test");
        }
        return value;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static int intEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
    }
}
