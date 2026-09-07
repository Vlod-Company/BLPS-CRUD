package ru.gigasigma.blpscrud.camunda.delegate;

import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.service.FlightService;

@Component("validatePurchaseFormListener")
@RequiredArgsConstructor
public class ValidatePurchaseFormListener implements TaskListener {

    private final Validator validator;
    private final FlightService flightService;

    @Override
    public void notify(DelegateTask task) {
        switch (task.getTaskDefinitionKey()) {
            case "Task_User_Search" -> validateSearch(task);
            case "Task_User_Select" -> validatePurchase(task);
            default -> throw new IllegalArgumentException("Unknown purchase form: " + task.getTaskDefinitionKey());
        }
    }

    private void validateSearch(DelegateTask task) {
        Long flightId = flightId(task);
        if (flightId != null) {
            validateFlight(flightId);
            return;
        }
        String from = text(task, "from");
        String to = text(task, "to");
        if (from == null || !from.matches("[A-Z]{3}") || to == null || !to.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Укажите ID рейса или аэропорты from/to (три заглавные латинские буквы) и дату.");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("Аэропорты отправления и прибытия должны различаться.");
        }
        try {
            String value = text(task, "date");
            if (value == null || LocalDate.parse(value).isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Дата вылета обязательна и не должна быть в прошлом.");
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Дата вылета должна иметь формат YYYY-MM-DD.");
        }
    }

    private void validatePurchase(DelegateTask task) {
        String method = text(task, "method");
        if (!"internal".equals(method) && !"redirect".equals(method)) {
            throw new IllegalArgumentException("Способ покупки: internal или redirect.");
        }
        SeatClass seatClass;
        try {
            seatClass = SeatClass.valueOf(String.valueOf(task.getVariable("seatClass")));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Выберите класс места: ECONOMY или BUSINESS.");
        }
        Object baggage = task.getVariable("hasBaggage");
        if (!(baggage instanceof Boolean)) {
            throw new IllegalArgumentException("hasBaggage должен быть логическим значением true или false.");
        }
        var request = new StartPurchaseRequest(flightId(task), text(task, "currency"),
                text(task, "seatNumber"), seatClass, (Boolean) baggage,
                text(task, "passengerName"), text(task, "passengerPassport"), text(task, "provider"));
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Проверьте поля формы: " + violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().collect(Collectors.joining("; ")));
        }
        validateFlight(request.flightId());
    }

    private void validateFlight(Long id) {
        var flight = flightService.getById(id);
        if (flight.getDepartureTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Рейс уже вылетел. Выберите другой рейс.");
        }
        if (flight.getAvailableSeats() == null || flight.getAvailableSeats() < 1) {
            throw new IllegalArgumentException("На выбранном рейсе нет свободных мест.");
        }
    }

    private Long flightId(DelegateTask task) {
        Object value = task.getVariable("flightId");
        if (value == null || "".equals(value)) {
            return null;
        }
        try {
            long id = new BigDecimal(value.toString()).longValueExact();
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException | ArithmeticException ignored) {
        }
        throw new IllegalArgumentException("ID рейса должен быть целым положительным числом.");
    }

    private String text(DelegateTask task, String name) {
        Object value = task.getVariable(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(name + " должен быть строкой.");
        }
        return text;
    }
}
