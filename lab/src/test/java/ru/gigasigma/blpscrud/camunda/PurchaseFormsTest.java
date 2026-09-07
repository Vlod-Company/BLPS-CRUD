package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.gigasigma.blpscrud.camunda.delegate.AssignProcessOwnerTaskListener;
import ru.gigasigma.blpscrud.camunda.delegate.InitializePurchaseListener;
import ru.gigasigma.blpscrud.camunda.delegate.ShowFlightsDelegate;
import ru.gigasigma.blpscrud.camunda.delegate.ValidatePurchaseFormListener;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;
import ru.gigasigma.blpscrud.service.CamundaIdentitySyncService;
import ru.gigasigma.blpscrud.service.CurrentUserService;
import ru.gigasigma.blpscrud.service.FlightService;

class PurchaseFormsTest {
    private ProcessEngine engine;
    private ValidatorFactory validators;
    private CurrentUserService currentUser;

    @BeforeEach
    void setUp() {
        var config = new StandaloneInMemProcessEngineConfiguration();
        config.setJdbcUrl("jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        config.setJobExecutorActivate(false);
        config.setAuthorizationEnabled(true);
        Map<Object, Object> beans = new HashMap<>();
        config.setBeans(beans);
        engine = config.buildProcessEngine();
        validators = Validation.buildDefaultValidatorFactory();
        var store = mock(XmlUserStore.class);
        currentUser = mock(CurrentUserService.class);
        var flights = mock(FlightService.class);
        var flight = new Flight();
        flight.setId(1L);
        flight.setAirline(mock(ru.gigasigma.blpscrud.entity.Airline.class));
        flight.setDepartureTime(LocalDateTime.now().plusDays(5));
        flight.setAvailableSeats(10);
        when(flights.getById(1L)).thenReturn(flight);
        when(flights.search(org.mockito.ArgumentMatchers.eq("LED"), org.mockito.ArgumentMatchers.eq("SVO"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(1)))
                .thenReturn(List.of(flight));
        beans.put("initializePurchaseListener", new InitializePurchaseListener(engine.getIdentityService(), currentUser, store));
        beans.put("assignProcessOwnerTaskListener", new AssignProcessOwnerTaskListener());
        beans.put("validatePurchaseFormListener", new ValidatePurchaseFormListener(validators.getValidator(), flights));
        beans.put("showFlightsDelegate", new ShowFlightsDelegate(flights));
        beans.put("createOrderDelegate", (JavaDelegate) execution -> execution.setVariable("orderId", 123L));
        var sync = new CamundaIdentitySyncService(engine.getIdentityService(), engine.getAuthorizationService(), store);
        ReflectionTestUtils.setField(sync, "defaultUserPassword", "test");
        for (String login : List.of("alice", "bob")) {
            var account = new XmlAccount(login.equals("alice") ? 1L : 2L, login, "hash", "ROLE_USER", login);
            when(store.findByLogin(login)).thenReturn(Optional.of(account));
            sync.syncRegisteredUser(account, "test");
        }
        engine.getRepositoryService().createDeployment()
                .addClasspathResource("processes/aviasales-camunda7.bpmn").deploy();
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
    void tasklistStartUsesAuthenticatedOwnerAndHidesTaskFromOtherUsers() {
        Task task = start();
        assertThat(task.getAssignee()).isEqualTo("alice");
        assertThat(task.getOwner()).isEqualTo("alice");
        assertThat(engine.getTaskService().getVariable(task.getId(), "userId")).isEqualTo(1L);
        assertThat(engine.getFormService().getRenderedTaskForm(task.getId())).isNotNull();
        engine.getIdentityService().setAuthentication("bob", List.of(CamundaIdentitySyncService.USER_GROUP));
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
    }

    @Test
    void invalidSearchRollsBackAndValidSearchOpensPurchaseForm() {
        Task task = start();
        assertThatThrownBy(() -> engine.getFormService().submitTaskForm(task.getId(),
                Map.of("from", "LED", "to", "LED", "date", "2030-01-01")))
                .hasStackTraceContaining("должны различаться");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(task.getId());
        assertThat(engine.getTaskService().getVariable(task.getId(), "from")).isNull();
        engine.getFormService().submitTaskForm(task.getId(), Map.of("flightId", 1L));
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getTaskDefinitionKey()).isEqualTo("Task_User_Select");
    }

    @Test
    void routeSearchPersistsResultsAndRejectsMalformedInput() {
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
                "date", java.time.LocalDate.now().plusDays(5).toString()));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        assertThat(purchase.getTaskDefinitionKey()).isEqualTo("Task_User_Select");
        assertThat((List<?>) engine.getTaskService().getVariable(purchase.getId(), "availableFlights")).hasSize(1);
        assertThat(engine.getFormService().getRenderedTaskForm(purchase.getId()).toString())
                .contains("cam-variable-name=\"availableFlightsSummary\" disabled");
        assertThat(engine.getFormService().getTaskFormVariables(purchase.getId())
                .get("availableFlightsSummary").toString()).contains("ID 1:");
    }

    @Test
    void invalidPurchaseCannotAdvanceAndValidPurchaseWaitsForPayment() {
        Task search = start();
        engine.getFormService().submitTaskForm(search.getId(), Map.of("flightId", 1L));
        Task purchase = engine.getTaskService().createTaskQuery().singleResult();
        var values = new HashMap<String, Object>(Map.of("flightId", 1L, "method", "internal", "currency", "RUB",
                "seatNumber", "BAD", "seatClass", "ECONOMY", "hasBaggage", false,
                "passengerName", "Ivan Ivanov", "passengerPassport", "1234 567890", "provider", "internal-bank"));
        assertThatThrownBy(() -> engine.getFormService().submitTaskForm(purchase.getId(), values))
                .hasStackTraceContaining("seatNumber");
        assertThat(engine.getTaskService().createTaskQuery().singleResult().getId()).isEqualTo(purchase.getId());
        values.put("seatNumber", "12A");
        engine.getFormService().submitTaskForm(purchase.getId(), values);
        assertThat(engine.getTaskService().createTaskQuery().count()).isZero();
        engine.getIdentityService().clearAuthentication();
        assertThat(engine.getRuntimeService().createEventSubscriptionQuery().eventName("PaymentReceived").count()).isEqualTo(1);
    }
}
