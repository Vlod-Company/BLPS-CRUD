package ru.gigasigma.blpscrud.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProcessPaymentRequest(
        @NotBlank(message = "cardNumber обязателен")
        @Pattern(regexp = "^[0-9]{12,19}$", message = "cardNumber должен содержать от 12 до 19 цифр")
        String cardNumber,

        @NotBlank(message = "cardHolder обязателен")
        @Size(min = 2, max = 64, message = "cardHolder должен быть длиной от 2 до 64 символов")
        @Pattern(regexp = "^[\\p{L} .'-]{2,64}$", message = "cardHolder содержит недопустимые символы")
        String cardHolder,

        @NotBlank(message = "cvv обязателен")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "cvv должен содержать 3 или 4 цифры")
        String cvv,

        boolean forceFail
) {
}
