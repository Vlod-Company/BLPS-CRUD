package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface OrderManagementService {
    WorkflowResult cancelOrder(Long orderId);
}