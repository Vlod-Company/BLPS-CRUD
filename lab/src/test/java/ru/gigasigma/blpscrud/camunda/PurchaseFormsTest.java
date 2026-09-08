package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import ru.gigasigma.blpscrud.camunda.delegate.AssignProcessOwnerTaskListener;
import ru.gigasigma.blpscrud.camunda.delegate.BookingDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.EmailDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.ExportCrmDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.ExternalPurchaseDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.InitializePurchaseListener;
import ru.gigasigma.blpscrud.camunda.delegate.ProcessPaymentDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.SaveTicketDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.ShowFlightsDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.ValidatePurchaseFormListener;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.entity.Airline;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;
import ru.gigasigma.blpscrud.service.CamundaIdentitySyncService;
import ru.gigasigma.blpscrud.service.CurrentUserService;
import ru.gigasigma.blpscrud.service.ExternalProcessCallbackService;
import ru.gigasigma.blpscrud.service.FlightService;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.PaymentCallbackProcessingService;
import ru.gigasigma.blpscrud.service.crm.LaxoCrmExportService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.AirlineBookingService;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.ticketDelivery.TicketDeliveryService;

class PurchaseFormsTest {
    private ProcessEngine engine;
    private ValidatorFactory validators;
    private CurrentUserService currentUser;
    private Map<Object, Object> beans;
    private ExternalPurchaseService externalPurchases;

    @BeforeEach
    void setUp() throws IOException {
        var config = new StandaloneInMemProcessEngineConfiguration();
        config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        config.setJobExecutorActivate(false);
        config.setAuthorizationEnabled(true);
        config.setProcessEnginePlugins(List.of(new PurchaseValidationPlugin()));
        beans = new HashMap<>();
        config.setBeans(beans);
        engine = config.buildProcessEngine();
        validators = Validation.buildDefaultValidatorFactory();
        var store = mock(XmlUserStore.class);
        currentUser = mock(CurrentUserService.class);
        var flights = mock(FlightService.class);
        var flight = new Flight();
        flight.setId(1L);
        flight.setAirline(mock(Airline.class));
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));
        flight.setAvailableSeats(10);
        when(flights.getById(1L)).thenReturn(flight);
        when(flights.search(ArgumentMatchers.eq("LED"), ArgumentMatchers.eq("SVO"),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(1)))
                .thenReturn(List.of(flight));
        beans.put("initializePurchaseListener", new InitializePurchaseListener(engine.getIdentityService(), currentUser, store));
        beans.put("assignProcessOwnerTaskListener", new AssignProcessOwnerTaskListener());
        beans.put("validatePurchaseFormListener", new ValidatePurchaseFormListener(validators.getValidator(), flights));
        beans.put("showFlightsDelegate", new ShowFlightsDelegate(flights,
                new ObjectMapper().findAndRegisterModules()));
        beans.put("createOrderDelegate", (JavaDelegate) execution -> execution.setVariable("orderId", 123L));
        beans.put("redirectDelegate", (JavaDelegate) execution -> {
            execution.setVariable("redirectUrl", "https://airline.example/payment/session-1");
            execution.setVariable("externalSessionId", "session-1");
        });
        externalPurchases = mock(ExternalPurchaseService.class);
        beans.put("externalPurchaseDelegate", new ExternalPurchaseDelegate(
                externalPurchases, new ObjectMapper(), validators.getValidator()));
        beans.put("saveTicketDelegate", (JavaDelegate) execution -> execution.setVariable("ticketSaved", true));
        beans.put("emailDelegate", (JavaDelegate) execution -> execution.setVariable("ticketDelivered", true));
        var sync = new CamundaIdentitySyncService(engine.getIdentityService(), engine.getAuthorizationService(), store);
        ReflectionTestUtils.setField(sync, "defaultUserPassword", "test");
        for (String login : List.of("alice", "bob")) {
            var account = new XmlAccount(login.equals("alice") ? 1L : 2L, login, "hash", "ROLE_USER", login);
            when(store.findByLogin(login)).thenReturn(Optional.of(account));
            sync.syncRegisteredUser(account, "test");
        }
        var deploymentConfig = new SpringProcessEngineConfiguration();
        deploymentConfig.setDeploymentResources(new Resource[] {
                new ClassPathResource("processes/aviasales-camunda7.bpmn"),
                new ClassPathResource("forms/flight-search.html"),
                new ClassPathResource("forms/flight-selection.html"),
                new ClassPathResource("forms/payment-link.html")
        });
        var deployment = engine.getRepositoryService().createDeployment();
        for (var resource : deploymentConfig.getDeploymentResources()) {
            try (var source = resource.getInputStream()) {
                deployment.addInputStream(resource.getFilename(), source);
            }
        }
        deployment.deploy();
        engine.getIdentityService().setAuthentication("alice", List.of(CamundaIdentitySyncService.USER_GROUP));
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.close();
        }
        if (validators != null) {
            validators.close();
        }
    }

    private Task start() {
        var definition = engine.getRepositoryService().createProcessDefinitionQuery().startableInTasklist().singleResult();
        assertThat(definition).isNotNull();
        engine.getFormService().submitStartForm(definition.getId(), Map.of("processOwnerLogin", "bob", "userId", 2L));
        return engine.getTaskService().createTaskQuery().singleResult();
    }

    @Test
    void tasklistStartUsesAuthenticatedOwnerAndHidesTaskFromOtherUsers() throws IOException {
        Task task = start();
        assertThat(task.getAssignee()).isEqualTo("alice");
        assertThat(task.getOwner()).isEqualTo("alice");
        assertThat(engine.getTaskService().getVariable(task.getId(), "userId")).isEqualTo(1L);
        try (var form = new ClassPathResource("forms/flight-search.html").getInputStream()) {
            assertThat(new String(form.readAllBytes(), StandardCharsets.UTF_8))
                    .contains("flightSearchForm", "cam-variable-name=\"date\"");
        }
        engine.getIdentityService().setAuthentication("bob", List.of(CamundaIdentitySyncService.USER_GROUP));
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
    }

    @Test
    void invalidSearchRollsBackAndValidSearchOpensPurchaseForm() {
        Task task = start();
        assertThatThrownBy(() -> engine.getFormService().submitTaskForm(task.getId(),
                Map.of("from", "LED", "to", "LED", "date", "2030-01-01")))
                .isInstanceOf(FormFieldValidationException.class)
                .hasMessage("Аэропорты отправления и прибытия должны различаться.")
                .hasStackTraceContaining("должны различаться");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(task.getId());
        assertThat(engine.getTaskService().getVariable(task.getId(), "from")).isNull();
        engine.getFormService().submitTaskForm(task.getId(), Map.of("flightId", 1L));
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getTaskDefinitionKey()).isEqualTo("Task_User_Flight_Select");
    }

    @Test
    void routeSearchPersistsResultsAndRejectsMalformedInput() throws IOException {
        Task task = start();
        for (Map<String, Object> values : List.<Map<String, Object>>of(
                Map.of("from", "LED", "to", "SVO", "date", "not-a-date"),
                Map.of("from", "LED", "to", "SVO", "date", "2000-01-01"),
                Map.of("from", "bad airport", "to", "SVO", "date", "2030-01-01"),
                Map.of("flightId", 1.5))) {
            assertThatThrownBy(() -> engine.getTaskService().complete(task.getId(), values)).isInstanceOf(RuntimeException.class);
            assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(task.getId());
        }
        engine.getFormService().submitTaskForm(task.getId(), Map.of("from", "LED", "to", "SVO",
                "date", LocalDate.now().plusDays(5).toString()));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(purchase.getTaskDefinitionKey()).isEqualTo("Task_User_Flight_Select");
        assertThat((List<?>) engine.getTaskService().getVariable(purchase.getId(), "availableFlights")).hasSize(1);
        assertThat(engine.getFormService().getTaskFormData(purchase.getId()).getFormKey())
                .isEqualTo("embedded:deployment:flight-selection.html");
        try (var form = new ClassPathResource("forms/flight-selection.html").getInputStream()) {
            assertThat(new String(form.readAllBytes(), StandardCharsets.UTF_8))
                    .contains("flightSelectionForm", "cam-variable-name=\"hasSuitableFlight\"");
        }
        assertThat(engine.getFormService().getTaskFormVariables(purchase.getId())
                .get("availableFlightsSummary").toString()).contains("ID 1:");
    }

    @Test
    void invalidPurchaseCannotAdvanceAndValidPurchaseWaitsForPayment() {
        Task search = start();
        engine.getFormService().submitTaskForm(search.getId(), Map.of("flightId", 1L));
        Task selection = engine.getTaskService().createTaskQuery().singleResult();
        engine.getTaskService().complete(selection.getId(), Map.of("hasSuitableFlight", "true", "flightId", 1L));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        var values = new HashMap<String, Object>(Map.of("flightId", 1L, "method", "internal", "currency", "RUB",
                "seatNumber", "BAD", "seatClass", "ECONOMY", "hasBaggage", false,
                "passengerName", "Ivan Ivanov", "passengerPassport", "1234 567890", "provider", "internal-bank"));
        assertThatThrownBy(() -> engine.getFormService().submitTaskForm(purchase.getId(), values))
                .hasStackTraceContaining("seatNumber");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(purchase.getId());
        values.put("seatNumber", "12A");
        values.put("flightId", 999L);
        assertThatThrownBy(() -> engine.getFormService().submitTaskForm(purchase.getId(), values))
                .hasStackTraceContaining("Выберите рейс из результатов поиска");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(purchase.getId());
        values.put("flightId", 1L);
        engine.getFormService().submitTaskForm(purchase.getId(), values);
        Task paymentLink = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(paymentLink.getTaskDefinitionKey()).isEqualTo("Task_User_Payment_Redirect");
        engine.getTaskService().complete(paymentLink.getId());
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
        engine.getIdentityService().clearAuthentication();
        assertThat(engine.getRuntimeService().createEventSubscriptionQuery().eventName("PaymentReceived").count()).isEqualTo(1);
    }

    @Test
    void externalPurchaseShowsOwnerLinkTaskBeforeWaitingForCallback() {
        Task search = start();
        engine.getTaskService().complete(search.getId(), Map.of("flightId", 1L));
        Task selection = engine.getTaskService().createTaskQuery().singleResult();
        engine.getTaskService().complete(selection.getId(), Map.of("hasSuitableFlight", "true", "flightId", 1L));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        engine.getFormService().submitTaskForm(purchase.getId(), Map.of(
                "flightId", 1L, "method", "redirect", "currency", "RUB",
                "seatNumber", "12A", "seatClass", "ECONOMY", "hasBaggage", false,
                "passengerName", "Ivan Ivanov", "passengerPassport", "1234 567890", "provider", "airline"));

        Task link = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(link.getTaskDefinitionKey()).isEqualTo("Activity_0o8m1af");
        assertThat(link.getAssignee()).isEqualTo("alice");
        assertThat(engine.getTaskService().getVariable(link.getId(), "redirectUrl"))
                .isEqualTo("https://airline.example/payment/session-1");
        engine.getIdentityService().setAuthentication("bob", List.of(CamundaIdentitySyncService.USER_GROUP));
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
        engine.getIdentityService().setAuthentication("alice", List.of(CamundaIdentitySyncService.USER_GROUP));
        engine.getTaskService().complete(link.getId());
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
        engine.getIdentityService().clearAuthentication();
        assertThat(engine.getRuntimeService().createEventSubscriptionQuery()
                .processInstanceId(search.getProcessInstanceId())
                .eventName("RedirectReceived").count()).isEqualTo(1);

        var callback = new ExternalBookingCallbackRequest(
                1L, 1L, "RUB", "12A", SeatClass.ECONOMY, false,
                "Ivan Ivanov", "1234 567890", "ivan@example.com", "+79991234567",
                "payment", "booking", "ticket", BigDecimal.TEN);
        var expected = new WorkflowResult(123L, OrderStatus.PAID, BigDecimal.TEN, "RUB", "completed", null);
        when(externalPurchases.completeExternalBookingForProcess(callback)).thenReturn(expected);
        var callbacks = new ExternalProcessCallbackService(engine.getRuntimeService(), new ObjectMapper());
        assertThatThrownBy(() -> callbacks.complete("wrong-session", callback))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(externalPurchases);
        assertThat(callbacks.complete("session-1", callback)).isEqualTo(expected);
        assertThat(engine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(search.getProcessInstanceId()).count()).isZero();
        verify(externalPurchases).completeExternalBookingForProcess(callback);
    }

    @Test
    void paymentRunsBookingCrmAndDeliveryInModelOrder() {
        var orders = mock(OrderService.class);
        var repository = mock(OrderRepository.class);
        var airline = mock(AirlineBookingService.class);
        var crm = mock(LaxoCrmExportService.class);
        var delivery = mock(TicketDeliveryService.class);
        var order = new Order();
        order.setId(123L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.TEN);
        var ticket = new Ticket();
        ticket.setId(321L);
        when(orders.getOrder(123L)).thenReturn(order);
        when(orders.getOrderTicket(123L)).thenReturn(ticket);
        beans.put("processPaymentDelegate", new ProcessPaymentDelegate(orders));
        beans.put("bookingDelegate", new BookingDelegate(orders, repository, airline));
        beans.put("exportCrmDelegate", new ExportCrmDelegate(orders, crm));
        beans.put("saveTicketDelegate", new SaveTicketDelegate(orders));
        beans.put("emailDelegate", new EmailDelegate(orders, delivery));
        Task search = start();
        engine.getTaskService().complete(search.getId(), Map.of("flightId", 1L));
        Task selection = engine.getTaskService().createTaskQuery().singleResult();
        engine.getTaskService().complete(selection.getId(), Map.of("hasSuitableFlight", "true", "flightId", 1L));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        engine.getFormService().submitTaskForm(purchase.getId(), Map.of(
                "flightId", 1L, "method", "internal", "currency", "RUB", "seatNumber", "12A",
                "seatClass", "ECONOMY", "hasBaggage", false, "passengerName", "Ivan Ivanov",
                "passengerPassport", "1234 567890", "provider", "internal-bank"));
        engine.getTaskService().complete(engine.getTaskService().createTaskQuery().singleResult().getId());
        engine.getIdentityService().clearAuthentication();
        var legacy = mock(InternalPurchaseService.class);
        new PaymentCallbackProcessingService(legacy, engine.getRuntimeService())
                .handleCallback(new PaymentCallbackRequest(123L, true, "payment", null, BigDecimal.TEN));

        InOrder sequence = inOrder(airline, repository, crm, delivery);
        sequence.verify(airline).issueTicket(order, ticket);
        sequence.verify(repository).save(order);
        sequence.verify(crm).exportSuccessfulPurchase(order, ticket);
        sequence.verify(delivery).sendTicket(order, ticket);
        verifyNoInteractions(legacy);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(engine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(search.getProcessInstanceId()).count()).isZero();
    }

    @Test
    void selectionRejectsMissingDecisionAndFlightOutsideSearchResults() {
        Task search = start();
        engine.getTaskService().complete(search.getId(), Map.of("flightId", 1L));
        Task selection = engine.getTaskService().createTaskQuery().singleResult();
        assertThatThrownBy(() -> engine.getTaskService().complete(selection.getId()))
                .hasStackTraceContaining("Укажите, есть ли подходящий рейс");
        assertThatThrownBy(() -> engine.getTaskService().complete(selection.getId(),
                Map.of("hasSuitableFlight", "true", "flightId", 999L)))
                .hasStackTraceContaining("Выберите рейс из результатов поиска");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(selection.getId());
        assertThat(engine.getTaskService().getVariable(selection.getId(), "flightId")).isEqualTo(1L);
    }

    @Test
    void rejectingFlightsReturnsToSearchWithinSameProcess() {
        Task search = start();
        engine.getTaskService().complete(search.getId(), Map.of("flightId", 1L));
        Task selection = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(selection.getTaskDefinitionKey()).isEqualTo("Task_User_Flight_Select");

        engine.getTaskService().complete(selection.getId(), Map.of("hasSuitableFlight", "false"));

        Task repeatedSearch = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(repeatedSearch.getTaskDefinitionKey()).isEqualTo("Task_User_Search");
        assertThat(repeatedSearch.getProcessInstanceId()).isEqualTo(search.getProcessInstanceId());
        assertThat(repeatedSearch.getAssignee()).isEqualTo("alice");
        assertThat(engine.getTaskService().getVariable(repeatedSearch.getId(), "flightId")).isNull();
    }
}
