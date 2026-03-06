package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.dto.StartPurchaseCommand;

public interface PurchaseWorkflowService {
    Order startInternalPurchase(StartPurchaseCommand command);
}
