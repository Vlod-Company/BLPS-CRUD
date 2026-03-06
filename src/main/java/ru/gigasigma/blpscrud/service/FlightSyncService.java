package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.entity.Flight;

public interface FlightSyncService {
    void refreshCatalog();
    Flight refreshFlightForPurchase(Long flightId);
}
