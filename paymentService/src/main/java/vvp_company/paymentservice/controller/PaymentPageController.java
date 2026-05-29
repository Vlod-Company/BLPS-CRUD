package vvp_company.paymentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vvp_company.paymentservice.dto.PaymentPageRequest;
import vvp_company.paymentservice.service.PaymentService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentPageController {

    private final PaymentService paymentService;


    @GetMapping
    public String paymentPage(
            @RequestParam String session,
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String replyTo,
            Model model
    ) {

        model.addAttribute("session", session);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("currency", currency);
        model.addAttribute("replyTo", replyTo);

        return "payment-page";
    }

    @PostMapping
    public String processPayment(
            @RequestParam String session,
            @RequestParam Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam String replyTo
    ) throws JsonProcessingException {

        var request = new PaymentPageRequest(
                session,
                orderId,
                amount,
                currency,
                replyTo
        );

        var success = paymentService.processPayment(request);

        return success
                ? "payment-success"
                : "payment-failed";
    }
}