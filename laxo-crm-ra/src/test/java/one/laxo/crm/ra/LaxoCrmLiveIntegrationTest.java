package one.laxo.crm.ra;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.security.auth.Subject;
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
                (LaxoCrmConnectionFactory) managedConnectionFactory.createConnectionFactory(new LocalConnectionManager());

        CrmPurchaseExportRequest request = purchaseRequest();
        try (var connection = connectionFactory.getConnection()) {
            CrmPurchaseExportResult result = connection.exportTicketPurchase(request);

            assertTrue(result.success(), result.message());
            assertNotNull(result.contactId(), "CRM contact id must be returned");
            assertFalse(result.contactId().isBlank(), "CRM contact id must not be blank");
            assertNotNull(result.dealId(), "CRM deal id must be returned");
            assertFalse(result.dealId().isBlank(), "CRM deal id must not be blank");

            System.out.printf(
                    "Laxo CRM live export created contactId=%s, dealId=%s, orderId=%s%n",
                    result.contactId(),
                    result.dealId(),
                    request.orderId()
            );
        }
    }

    private static CrmPurchaseExportRequest purchaseRequest() {
        long uniqueId = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        return new CrmPurchaseExportRequest(
                uniqueId,
                1000L + uniqueId % 100000,
                now,
                new BigDecimal("12345.67"),
                "RUB",
                "PAID",
                "INTERNAL",
                "live-jca-test-" + uniqueId,
                uniqueId + 1,
                "12A",
                "ECONOMY",
                true,
                "BLPS JCA Test " + uniqueId,
                "4510 123456",
                777L,
                "BLPS-" + uniqueId % 10000,
                "LED",
                "SVO",
                now.plusDays(3),
                now.plusDays(3).plusHours(2),
                "Airbus A320",
                new BigDecimal("10000.00"),
                "BLPS Test Airlines",
                "BT",
                "RU",
                "https://example.com/blps-jca-test"
        );
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

    private static final class LocalConnectionManager implements ConnectionManager {

        @Override
        public Object allocateConnection(
                ManagedConnectionFactory managedConnectionFactory,
                ConnectionRequestInfo connectionRequestInfo
        ) throws ResourceException {
            var managedConnection = managedConnectionFactory.createManagedConnection(new Subject(), connectionRequestInfo);
            return managedConnection.getConnection(new Subject(), connectionRequestInfo);
        }
    }
}
