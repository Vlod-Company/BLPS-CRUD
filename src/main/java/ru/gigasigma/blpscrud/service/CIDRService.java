package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CIDRService {

    private final NetworkPoliticsService networkPoliticsService;

    public List<String> getValidRole(String ipAddress, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        String normalizedIpAddress = normalizeIpAddress(ipAddress);
        if (normalizedIpAddress == null) {
            log.warn("Cannot resolve client IP from value '{}'", ipAddress);
            return List.of();
        }

        List<String> grantedRoles = new ArrayList<>(roles);
        List<String> matchedPolicies = new ArrayList<>();

        for (NetworkPolitics policy : networkPoliticsService.findAll()) {
            if (!matchesPolicy(policy, normalizedIpAddress)) {
                continue;
            }

            matchedPolicies.add(policy.getName());
            grantedRoles.retainAll(policy.getRoles() != null ? policy.getRoles() : List.of());

            if (grantedRoles.isEmpty()) {
                break;
            }
        }

        if (matchedPolicies.isEmpty()) {
            log.info("No network policy matched ip '{}'; roles denied", normalizedIpAddress);
            return List.of();
        }

        log.info("Ip '{}' matched policies {} and resolved roles {}", normalizedIpAddress, matchedPolicies, grantedRoles);
        return grantedRoles;
    }

    private boolean matchesPolicy(NetworkPolitics policy, String ipAddress) {
        if (policy.getAddresses() == null || policy.getAddresses().isEmpty()) {
            return false;
        }

        return policy.getAddresses().stream()
                .map(NetworkPolitics.NetworkAddress::getAddr)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(addr -> !addr.isEmpty())
                .anyMatch(addr -> matchesAddress(addr, ipAddress, policy.getName()));
    }

    private boolean matchesAddress(String cidr, String ipAddress, String policyName) {
        try {
            return new IpAddressMatcher(cidr).matches(ipAddress);
        } catch (IllegalArgumentException ex) {
            log.warn("Skipping invalid network address '{}' in policy '{}'", cidr, policyName);
            return false;
        }
    }

    private String normalizeIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }

        for (String candidate : ipAddress.split(",")) {
            String normalized = candidate.trim();
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }

        return null;
    }
}
