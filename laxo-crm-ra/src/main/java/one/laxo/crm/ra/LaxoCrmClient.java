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

    public LaxoCrmClient(LaxoCrmClientConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout(config.connectTimeoutMillis(), 5000)))
                .build();
    }

    public CrmPurchaseExportResult exportTicketPurchase(CrmPurchaseExportRequest request) {
        requireConfigured();
        CrmContactResult contact = createOrUpdateContact(new CrmContactRequest(
                request.passengerName(),
                request.userId(),
                maskDocument(request.passengerPassport()),
                SOURCE
        ));
        if (!contact.success()) {
            return new CrmPurchaseExportResult(false, null, null, contact.message());
        }

        CrmDealRequest dealRequest = new CrmDealRequest(
                "Ticket purchase #" + request.orderId(),
                request.totalPrice(),
                request.currency(),
                request.orderId(),
                request.orderStatus(),
                request.paymentMethod(),
                request.flightNumber(),
                joinRoute(request.departureAirport(), request.arrivalAirport()),
                request.departureTime(),
                request.arrivalTime(),
                request.seatNumber(),
                request.seatClass(),
                request.hasBaggage(),
                request.airlineName(),
                contact.contactId()
        );

        CrmDealResult deal = createDeal(dealRequest);
        return new CrmPurchaseExportResult(
                deal.success(),
                contact.contactId(),
                deal.dealId(),
                deal.success() ? "Ticket purchase exported to Laxo CRM" : deal.message()
        );
    }

    public CrmContactResult createOrUpdateContact(CrmContactRequest request) {
        requireConfigured();
        String externalUserIdFieldId = ensureCustomField("BLPS User ID", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);
        String documentFieldId = ensureCustomField("BLPS Passenger Document", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);
        String sourceFieldId = ensureCustomField("BLPS Source", CONTACT_SCOPE_ID, CONTACT_SCOPE_NAME);

        Map<String, Object> params = mapOf(
                "contact_name", required(request.fullName(), "contact fullName"),
                "field", List.of(
                        fieldValue(externalUserIdFieldId, stringValue(request.externalUserId())),
                        fieldValue(documentFieldId, request.documentNumber()),
                        fieldValue(sourceFieldId, request.source())
                )
        );
        Map<String, Object> command = requestItem("contact", "add", params);
        String contactId = sendAndReadId(List.of(command), "contact_id");
        return new CrmContactResult(true, contactId, "Laxo contact.add completed");
    }

    public CrmDealResult createDeal(CrmDealRequest request) {
        requireConfigured();
        List<Map<String, Object>> fields = List.of(
                orderField("BLPS Order ID", stringValue(request.externalOrderId())),
                orderField("BLPS Order Status", request.orderStatus()),
                orderField("BLPS Payment Method", request.paymentMethod()),
                orderField("BLPS Flight Number", request.flightNumber()),
                orderField("BLPS Route", request.route()),
                orderField("BLPS Departure Time", stringValue(request.departureTime())),
                orderField("BLPS Arrival Time", stringValue(request.arrivalTime())),
                orderField("BLPS Seat Number", request.seatNumber()),
                orderField("BLPS Seat Class", request.seatClass()),
                orderField("BLPS Has Baggage", stringValue(request.hasBaggage())),
                orderField("BLPS Airline", request.airlineName()),
                orderField("BLPS Currency", request.currency())
        );
        Map<String, Object> params = mapOf(
                "order_name", required(request.title(), "deal title"),
                "order_sum", stringValue(request.amount()),
                "order_status_id", stringValue(config.orderStatusId()),
                "funnel_id", stringValue(config.funnelId()),
                "contact_id", required(request.contactId(), "contactId"),
                "order_user_mentor", stringValue(config.orderUserMentor())
        );
        Map<String, Object> command = requestItem("order", "add", params);
        String dealId = sendAndReadId(List.of(command), "order_id");
        addOrderFields(dealId, fields);
        return new CrmDealResult(true, dealId, "Laxo order.add and order.add_field completed");
    }

    private Map<String, Object> requestItem(String className, String method, Object param) {
        return mapOf(
                "class", className,
                "method", method,
                "param", param,
                "sid", config.sid()
        );
    }

    private List<Map<String, Object>> requestBody(String className, String method, Object param) {
        return List.of(requestItem(className, method, param));
    }

    private String origin() {
        return "https://" + config.crmName() + ".laxo.one";
    }

    private String apiUrl() {
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
        String createdId = sendAndReadId(List.of(requestItem("field", "add", params)), "field_id");
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
            String name = text(node, "field_view_name");
            if (fieldName.equals(name)) {
                String id = text(node, "field_id");
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

    private Map<String, Object> orderField(String fieldName, Object value) {
        return fieldValue(ensureCustomField(fieldName, ORDER_SCOPE_ID, ORDER_SCOPE_NAME), value);
    }

    private void addOrderFields(String orderId, List<Map<String, Object>> fields) {
        List<Map<String, Object>> commands = fields.stream()
                .map(field -> requestItem("order", "add_field", mapOf(
                        "order_id", required(orderId, "dealId"),
                        "field_id", stringValue(field.get("field_id")),
                        "value", stringValue(field.get("value"))
                )))
                .toList();
        sendAndExpectSuccess(commands);
    }

    private String sendAndReadId(List<Map<String, Object>> commands, String responseIdField) {
        String response = send(commands);
        JsonNode root = readJson(response);
        ensureSuccess(root, response);
        String id = readFirstResponseId(root, responseIdField);
        if (id == null || id.isBlank() || "false".equals(id)) {
            throw new LaxoCrmResourceAccessException(
                    "Laxo CRM response does not contain " + responseIdField + ": " + response
            );
        }
        return id;
    }

    private void sendAndExpectSuccess(List<Map<String, Object>> commands) {
        String response = send(commands);
        ensureSuccess(readJson(response), response);
    }

    private static void ensureSuccess(JsonNode root, String response) {
        for (JsonNode item : responseItems(root)) {
            int code = item.get("code") == null ? -1 : item.get("code").asInt(-1);
            if (code != 200) {
                throw new LaxoCrmResourceAccessException("Laxo CRM request failed with code " + code + ": " + response);
            }
        }
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

    private static String readFirstResponseId(JsonNode root, String responseIdField) {
        JsonNode response = firstResponse(root);
        if (response == null || response.isNull() || response.isMissingNode()) {
            return null;
        }
        if (response.isArray() && !response.isEmpty()) {
            response = response.get(0);
        }
        if (response.isObject()) {
            return text(response, responseIdField);
        }
        return response.asText();
    }

    private static JsonNode firstItem(JsonNode root) {
        if (root == null) {
            return null;
        }
        return root.isArray() && !root.isEmpty() ? root.get(0) : root;
    }

    private static List<JsonNode> responseItems(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return List.of();
        }
        if (!root.isArray()) {
            return List.of(root);
        }
        List<JsonNode> result = new ArrayList<>();
        root.forEach(result::add);
        return result;
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

    private static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || value.isMissingNode() ? null : value.asText();
    }

    private static Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
