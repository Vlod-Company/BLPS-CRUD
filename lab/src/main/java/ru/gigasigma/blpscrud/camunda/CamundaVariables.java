package ru.gigasigma.blpscrud.camunda;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.enums.SeatClass;

public final class CamundaVariables {

    private CamundaVariables() {
    }

    public static Long longValue(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    public static String stringValue(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        return value == null ? null : value.toString();
    }

    public static Boolean booleanValue(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(value.toString());
    }

    public static BigDecimal decimalValue(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    public static LocalDate localDateValue(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        return LocalDate.parse(value.toString());
    }

    public static StartPurchaseRequest purchaseRequest(DelegateExecution execution) {
        return new StartPurchaseRequest(
                longValue(execution, "flightId"),
                stringValue(execution, "currency"),
                stringValue(execution, "seatNumber"),
                SeatClass.valueOf(stringValue(execution, "seatClass")),
                booleanValue(execution, "hasBaggage"),
                stringValue(execution, "passengerName"),
                stringValue(execution, "passengerPassport"),
                stringValue(execution, "provider")
        );
    }
}
