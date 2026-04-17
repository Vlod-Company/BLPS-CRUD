package ru.gigasigma.blpscrud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CIDRService {
    private static final Path CONFIG_PATH = Paths.get("./", "ip_address_white_list.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private IpWhiteListConfig ipWhiteListConfig;


    private record IpWhiteListConfig(
            List<IpAddresses> addresses
    ) {}

    private record IpAddresses(
            String addr,
            String description,
            List<String> roles
    ) {}

    @EventListener(ApplicationReadyEvent.class)
    private void setupIpAddressWhiteList() {
        if (!Files.exists(CONFIG_PATH)) {
            log.info("Ip config file does not exist");
            return;
        }

        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            IpWhiteListConfig IpWhiteListConfig = objectMapper.readValue(inputStream, IpWhiteListConfig.class);
            if (IpWhiteListConfig == null) {
                log.warn("Ip config file is invalid and will be ignored: {}", CONFIG_PATH.toAbsolutePath());
                return;
            }

            this.ipWhiteListConfig = IpWhiteListConfig;
            log.info("Ip config restored from {}", CONFIG_PATH.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to read Ip config from {}", CONFIG_PATH.toAbsolutePath(), e);
        }
    }

    public List<String> getValidRole(String ipAddress, List<String> roles) {
        IpAddressMatcher matcher;
        List<String> ipGrantedRoles = new ArrayList<>();
        List<IpAddresses> sortedIpAddresses = ipWhiteListConfig.addresses().stream().sorted(Comparator.comparing(a -> a.addr().split("/")[1])).toList();

        for (var ips: sortedIpAddresses) {
            matcher = new IpAddressMatcher(ips.addr());
            if (matcher.matches(ipAddress)) {
                return ips.roles();
            }
        }

        return List.of();
    }
}
