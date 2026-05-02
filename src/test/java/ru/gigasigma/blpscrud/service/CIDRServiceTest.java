package ru.gigasigma.blpscrud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CIDRServiceTest {

    @Mock
    private NetworkPoliticsService networkPoliticsService;

    @InjectMocks
    private CIDRService cidrService;

    @Test
    void getValidRoleKeepsAnyUserRoleAllowedByAnyMatchedPolicy() {
        NetworkPolitics userPolicy = policy(
                "users",
                List.of("ROLE_USER"),
                List.of("0.0.0.0/0")
        );
        NetworkPolitics adminPolicy = policy(
                "admins",
                List.of("ROLE_ADMIN"),
                List.of("0.0.0.0/0")
        );

        when(networkPoliticsService.findAll()).thenReturn(List.of(userPolicy, adminPolicy));

        List<String> grantedRoles = cidrService.getValidRole(
                "203.0.113.10",
                List.of("ROLE_USER", "ROLE_ADMIN")
        );

        assertThat(grantedRoles).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void getValidRoleDoesNotGrantRolesMissingFromAuthenticatedUser() {
        NetworkPolitics adminPolicy = policy(
                "admins",
                List.of("ROLE_ADMIN"),
                List.of("0.0.0.0/0")
        );

        when(networkPoliticsService.findAll()).thenReturn(List.of(adminPolicy));

        List<String> grantedRoles = cidrService.getValidRole(
                "203.0.113.10",
                List.of("ROLE_USER")
        );

        assertThat(grantedRoles).isEmpty();
    }

    private NetworkPolitics policy(String name, List<String> roles, List<String> cidrs) {
        NetworkPolitics policy = new NetworkPolitics();
        policy.setName(name);
        policy.setRoles(roles);
        policy.setAddresses(cidrs.stream().map(cidr -> {
            NetworkPolitics.NetworkAddress address = new NetworkPolitics.NetworkAddress();
            address.setAddr(cidr);
            address.setPolitics(policy);
            return address;
        }).toList());
        return policy;
    }
}
