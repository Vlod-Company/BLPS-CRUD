package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.localDateValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.stringValue;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.controller.dto.response.FlightResponse;
import ru.gigasigma.blpscrud.service.FlightService;

@Slf4j
@Component("showFlightsDelegate")
@RequiredArgsConstructor
public class ShowFlightsDelegate implements JavaDelegate {

    private final FlightService flightService;

    @Override
    public void execute(DelegateExecution execution) {
        Long selectedFlightId = longValue(execution, "flightId");
        if (selectedFlightId != null) {
            execution.setVariable("hasAvailableFlights", true);
            execution.setVariable("availableFlightCount", 1);
            log.info("Camunda flight search skipped because selected flight is already provided. flightId={}", selectedFlightId);
            return;
        }

        String from = stringValue(execution, "from");
        String to = stringValue(execution, "to");
        var date = localDateValue(execution, "date");

        if (from == null || to == null || date == null) {
            execution.setVariable("hasAvailableFlights", false);
            execution.setVariable("availableFlightCount", 0);
            log.info("Camunda flight search variables are incomplete and no selected flight id is provided.");
            return;
        }

        List<FlightResponse> flights = flightService.search(
                        from,
                        to,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay().minusSeconds(1),
                        1
                )
                .stream()
                .map(FlightResponse::fromEntity)
                .toList();
        execution.setVariable("hasAvailableFlights", !flights.isEmpty());
        execution.setVariable("availableFlightCount", flights.size());
        execution.setVariable("availableFlights", flights);
        log.info("Camunda flight search completed. from={}, to={}, count={}", from, to, flights.size());
    }
}
