package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CIDRService {

    private final NetworkPoliticsService networkPoliticsService;

    public void getValidRole(String ipAddress, List<String> roles) {

    }
}
