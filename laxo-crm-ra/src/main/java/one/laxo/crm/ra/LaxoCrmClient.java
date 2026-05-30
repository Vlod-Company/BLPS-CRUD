package one.laxo.crm.ra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import one.laxo.crm.api.CrmContactRequest;
import one.laxo.crm.api.CrmContactResult;
import one.laxo.crm.api.CrmDealRequest;
import one.laxo.crm.api.CrmDealResult;
import one.laxo.crm.api.CrmPurchaseExportRequest;
import one.laxo.crm.api.CrmPurchaseExportResult;

class LaxoCrmClient {

    private static final String SOURCE = "BLPS-CRUD";
    private static final int MODULE_ID = 1;
    private static final String CONTACT_SCOPE_NAME = "contact";
    private static final String ORDER_SCOPE_NAME = "order";
    private static final String CONTACT_SCOPE_ID = "1";
    private static final String ORDER_SCOPE_ID = "2";
    private static final String TEXT_FIELD_TYPE_ID = "2";
    private static final ConcurrentMap<String, String> CUSTOM_FIELD_CACHE = new ConcurrentHashMap<>();

    private final LaxoCrmClientConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    LaxoCrmClient(LaxoCrmClientConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout(config.connectTimeoutMillis(), 5000)))
                .build();
    }

    CrmPurchaseExportResult exportTicketPurchase(CrmPurchaseExportRequest request) {
        requireConfigured();
        CrmContactRequest contactRequest = new CrmContactRequest();
        contactRequest.setFullName(request.getPassengerName());
        contactRequest.setExternalUserId(request.getUserId());
        contactRequest.setDocumentNumber(maskDocument(request.getPassengerPassport()));
        contactRequest.setSource(SOURCE);

        CrmContactResult contact = createOrUpdateContact(contactRequest);
        if (!contact.isSuccess()) {
            return new CrmPurchaseExportResult(false, null, null, contact.getMessage());
        }

        CrmDealRequest dealRequest = new CrmDealRequest();
        dealRequest.setTitle("Ticket purchase #" + request.getOrderId());
        dealRequest.setAmount(request.getTotalPrice());
        dealRequest.setCurrency(request.getCurrency());
        dealRequest.setExternalOrderId(request.getOrderId());
        dealRequest.setOrderStatus(request.getOrderStatus());
        dealRequest.setPaymentMethod(request.getPaymentMethod());
        dealRequest.setFlightNumber(request.getFlightNumber());
        dealRequest.setRoute(joinRoute(request.getDepartureAirport(), request.getArrivalAirport()));
        dealRequest.setDepartureTime(request.getDepartureTime());
        dealRequest.setArrivalTime(request.getArrivalTime());
        dealRequest.setSeatNumber(request.getSeatNumber());
        dealRequest.setSeatClass(request.getSeatClass());
        dealRequest.setHasBaggage(request.getHasBaggage());
        dealRequest.setAirlineName(request.getAirlineName());
        dealRequest.setContactId(contact.getContactId());

        CrmDealResult deal = createDeal(dealRequest);
        return new CrmPurchaseExportResult(
                deal.isSuccess(),
                contact.getContactId(),
                deal.getDealId(),
                deal.isSuccess() ? "Ticket purchase exported to Laxo CRM" : deal.getMessage()
        );
    }

    CrmContactResult createOrUpdateContact(CrmContactRequest request) {
        requireConfigured();
        String externalUserIdFieldId = ensureCustomField("BLPS User ID", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);
        String documentFieldId = ensureCustomField("BLPS Passenger Document", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);
        String sourceFieldId = ensureCustomField("BLPS Source", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);

        Map<String, Object> params = mapOf(
                "contact_name", required(request.getFullName(), "contact fullName"),
                "field", List.of(
                        fieldValue(externalUserIdFieldId, stringValue(request.getExternalUserId())),
                        fieldValue(documentFieldId, request.getDocumentNumber()),
                        fieldValue(sourceFieldId, request.getSource())
                )
        );
        Map<String, Object> command = requestItem("contact", "add", params);
        String contactId = sendAndReadId(List.of(command));
        return new CrmContactResult(true, contactId, "Laxo contact.add completed");
    }

    CrmDealResult createDeal(CrmDealRequest request) {
        requireConfigured();
        List<Map<String, Object>> fields = List.of(
                fieldValue(ensureCustomField("BLPS Order ID", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), stringValue(request.getExternalOrderId())),
                fieldValue(ensureCustomField("BLPS Order Status", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getOrderStatus()),
                fieldValue(ensureCustomField("BLPS Payment Method", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getPaymentMethod()),
                fieldValue(ensureCustomField("BLPS Flight Number", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getFlightNumber()),
                fieldValue(ensureCustomField("BLPS Route", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getRoute()),
                fieldValue(ensureCustomField("BLPS Departure Time", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), stringValue(request.getDepartureTime())),
                fieldValue(ensureCustomField("BLPS Arrival Time", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), stringValue(request.getArrivalTime())),
                fieldValue(ensureCustomField("BLPS Seat Number", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getSeatNumber()),
                fieldValue(ensureCustomField("BLPS Seat Class", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getSeatClass()),
                fieldValue(ensureCustomField("BLPS Has Baggage", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), stringValue(request.getHasBaggage())),
                fieldValue(ensureCustomField("BLPS Airline", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getAirlineName()),
                fieldValue(ensureCustomField("BLPS Currency", ORDER_SCOPE_ID, ORDER_SCOPE_NAME), request.getCurrency())
        );
        Map<String, Object> params = mapOf(
                "order_name", required(request.getTitle(), "deal title"),
                "order_sum", stringValue(request.getAmount()),
                "order_status_id", stringValue(config.orderStatusId()),
                "funnel_id", stringValue(config.funnelId()),
                "contact_id", required(request.getContactId(), "contactId"),
                "order_user_mentor", stringValue(config.orderUserMentor()),
                "field", fields
        );
        Map<String, Object> command = requestItem("order", "add", params);
        String dealId = sendAndReadId(List.of(command));
        return new CrmDealResult(true, dealId, "Laxo order.add completed");
    }

    Map<String, Object> requestItem(String className, String method, Object param) {
        return mapOf(
                "class", className,
                "method", method,
                "param", param,
                "sid", config.sid()
        );
    }

    List<Map<String, Object>> requestBody(String className, String method, Object param) {
        return List.of(requestItem(className, method, param));
    }

    String origin() {
        return "https://" + config.crmName() + ".laxo.one";
    }

    String apiUrl() {
        return config.baseUrl();
    }

    private String ensureCustomField(String fieldName, String scopeId, String scopeName) {
        String cacheKey = config.baseUrl() + "|" + config.crmName() + "|" + scopeName + "|" + fieldName;
        String cachedId = CUSTOM_FIELD_CACHE.get(cacheKey);
        if (cachedId != null) {
            return cachedId;
        }

        String existingId = findCustomFieldId(fieldName, scopeName);
        if (existingId != null) {
            CUSTOM_FIELD_CACHE.putIfAbsent(cacheKey, existingId);
            return existingId;
        }
        Map<String, Object> params = mapOf(
                "field_name", fieldName,
                "field_view_name", fieldName,
                "field_scope_id", scopeId,
                "field_type_id", TEXT_FIELD_TYPE_ID,
                "field_min_count", 0,
                "field_max_count", 1,
                "field_icon_name", "",
                "field_priority", 0
        );
        String createdId = sendAndReadId(List.of(requestItem("field", "add", params)));
        CUSTOM_FIELD_CACHE.putIfAbsent(cacheKey, createdId);
        return createdId;
    }

    private String findCustomFieldId(String fieldName, String scopeName) {
        Map<String, Object> params = mapOf(
                "module_id", MODULE_ID,
                "scope_name", scopeName
        );
        String response = send(List.of(requestItem("field", "object_proto", params)));
        JsonNode root = readJson(response);
        JsonNode payload = firstResponse(root);
        for (JsonNode node : flattenObjects(payload)) {
            String name = firstText(node, "field_name", "field_view_name", "name", "view_name");
            if (fieldName.equals(name)) {
                String id = firstText(node, "field_id", "id");
                if (id != null && !id.isBlank()) {
                    return id;
                }
            }
        }
        return null;
    }

    private static Map<String, Object> fieldValue(String fieldId, Object value) {
        return mapOf(
                "field_id", fieldId,
                "value", value == null ? "" : value
        );
    }

    private String sendAndReadId(List<Map<String, Object>> commands) {
        String response = send(commands);
        JsonNode root = readJson(response);
        int code = readFirstCode(root);
        if (code != 200) {
            throw new LaxoCrmResourceAccessException("Laxo CRM request failed with code " + code + ": " + response);
        }
        String id = readFirstResponseScalar(root);
        if (id == null || id.isBlank() || "false".equals(id)) {
            throw new LaxoCrmResourceAccessException("Laxo CRM response does not contain created object id: " + response);
        }
        return id;
    }

    private String send(List<Map<String, Object>> commands) {
        try {
            String json = objectMapper.writeValueAsString(commands);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Origin", origin())
                    .header("Referer", origin())
                    .timeout(Duration.ofMillis(timeout(config.readTimeoutMillis(), 20000)))
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new LaxoCrmResourceAccessException("Laxo CRM HTTP status " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new LaxoCrmResourceAccessException("I/O error during Laxo CRM request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LaxoCrmResourceAccessException("Interrupted during Laxo CRM request", e);
        }
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new LaxoCrmResourceAccessException("Could not parse Laxo CRM response: " + json, e);
        }
    }

    private void requireConfigured() {
        required(config.baseUrl(), "baseUrl");
        required(config.sid(), "sid");
        required(config.crmName(), "crmName");
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new LaxoCrmResourceAccessException("Laxo CRM " + name + " is not configured");
        }
        return value;
    }

    private static String joinRoute(String departureAirport, String arrivalAirport) {
        return nullToEmpty(departureAirport) + " -> " + nullToEmpty(arrivalAirport);
    }

    private static String maskDocument(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return "";
        }
        String clean = documentNumber.replaceAll("\\s+", "");
        if (clean.length() <= 4) {
            return "****";
        }
        return "*".repeat(clean.length() - 4) + clean.substring(clean.length() - 4);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String stringValue(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private static String stringValue(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static long timeout(Integer value, long defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private static int readFirstCode(JsonNode root) {
        JsonNode first = firstItem(root);
        return first == null || first.get("code") == null ? -1 : first.get("code").asInt(-1);
    }

    private static String readFirstResponseScalar(JsonNode root) {
        JsonNode response = firstResponse(root);
        if (response == null || response.isNull() || response.isMissingNode()) {
            return null;
        }
        if (response.isArray() && !response.isEmpty()) {
            response = response.get(0);
        }
        if (response.isObject()) {
            String id = firstText(response, "id", "field_id", "contact_id", "order_id");
            return id == null ? response.toString() : id;
        }
        return response.asText();
    }

    private static JsonNode firstItem(JsonNode root) {
        if (root == null) {
            return null;
        }
        return root.isArray() && !root.isEmpty() ? root.get(0) : root;
    }

    private static JsonNode firstResponse(JsonNode root) {
        JsonNode first = firstItem(root);
        return first == null ? null : first.get("response");
    }

    private static List<JsonNode> flattenObjects(JsonNode node) {
        List<JsonNode> result = new ArrayList<>();
        collectObjects(node, result);
        return result;
    }

    private static void collectObjects(JsonNode node, List<JsonNode> result) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            result.add(node);
        }
        if (node.isContainerNode()) {
            node.forEach(child -> collectObjects(child, result));
        }
    }

    private static String firstText(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && !value.isMissingNode()) {
                return value.asText();
            }
        }
        return null;
    }

    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
