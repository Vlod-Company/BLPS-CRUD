package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.request.UserRequest;
import ru.gigasigma.blpscrud.controller.dto.response.LoginResponse;
import ru.gigasigma.blpscrud.controller.dto.response.RegisterResponse;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.service.AuthService;
import ru.gigasigma.blpscrud.service.ClientIpResolver;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid UserRequest request) {
        XmlAccount account = authService.register(request.login(), request.password());
        return RegisterResponse.fromAccount(account);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid UserRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, clientIpResolver.resolve(httpRequest));
    }
}
