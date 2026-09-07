package ru.gigasigma.blpscrud.security;

import org.springframework.security.authentication.jaas.AuthorityGranter;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class RoleAuthorityGranter implements AuthorityGranter {

    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof RolePrincipal rolePrincipal) {
            return Collections.singleton(rolePrincipal.getName());
        }
        return Collections.emptySet();
    }
}