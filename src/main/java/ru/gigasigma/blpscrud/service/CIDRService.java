package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CIDRService {

    private final NetworkPoliticsService networkPoliticsService;
    private final ClientIpResolver clientIpResolver;

    public List<String> getValidRoles(String ipAddress, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        List<MatchedPolicy> matchedPolicies = new ArrayList<>();
        int maxMask = -1;

        for (NetworkPolitics policy : networkPoliticsService.findAll()) {
            Integer matchedMask = resolveMatchingMask(policy, ipAddress);
            if (matchedMask == null) {
                continue;
            }

            matchedPolicies.add(new MatchedPolicy(policy, matchedMask));
            maxMask = Math.max(maxMask, matchedMask);
        }

        if (matchedPolicies.isEmpty()) {
            log.info("No network policy matched ip '{}'; roles denied", ipAddress);
            return List.of();
        }

        Set<String> allowedRoles = new LinkedHashSet<>();
        List<String> appliedPolicies = new ArrayList<>();

        for (MatchedPolicy matchedPolicy : matchedPolicies) {
            if (matchedPolicy.mask() != maxMask) {
                continue;
            }

            NetworkPolitics policy = matchedPolicy.policy();
            appliedPolicies.add(policy.getName());
            if (policy.getRoles() != null) {
                allowedRoles.addAll(policy.getRoles());
            }
        }

        List<String> grantedRoles = roles.stream()
                .filter(allowedRoles::contains)
                .toList();

        log.info("Ip '{}' matched policies {} on mask /{} and resolved roles {}", ipAddress, appliedPolicies, maxMask, grantedRoles);
        return grantedRoles;
    }

    private Integer resolveMatchingMask(NetworkPolitics policy, String ipAddress) {
        if (policy.getAddresses() == null || policy.getAddresses().isEmpty()) {
            return null;
        }

        return policy.getAddresses().stream()
                .map(NetworkPolitics.NetworkAddress::getAddr)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(addr -> !addr.isEmpty())
                .map(addr -> resolveMaskIfMatches(addr, ipAddress, policy.getName()))
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private Integer resolveMaskIfMatches(String cidr, String ipAddress, String policyName) {
        try {
            if (!new IpAddressMatcher(cidr).matches(ipAddress)) {
                return null;
            }

            int slashIndex = cidr.indexOf('/');
            if (slashIndex < 0 || slashIndex == cidr.length() - 1) {
                throw new IllegalArgumentException("CIDR mask is missing");
            }

            int mask = Integer.parseInt(cidr.substring(slashIndex + 1).trim());
            if (mask < 0 || mask > 32) {
                throw new IllegalArgumentException("CIDR mask is out of range");
            }

            return mask;
        } catch (IllegalArgumentException ex) {
            log.warn("Skipping invalid network address '{}' in policy '{}'", cidr, policyName);
            return null;
        }
    }

    private record MatchedPolicy(NetworkPolitics policy, int mask) {
    }
}
