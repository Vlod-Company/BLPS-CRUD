package ru.gigasigma.blpscrud.camunda;

public class PurchaseFormValidationException extends IllegalArgumentException {
    public PurchaseFormValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
