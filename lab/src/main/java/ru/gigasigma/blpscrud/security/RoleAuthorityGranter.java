package ru.gigasigma.blpscrud.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import org.springframework.security.authentication.jaas.AuthorityGranter;

public class RoleAuthorityGranter implements AuthorityGranter {

    @Override
    public Set<String> grant(Principal principal) {
        if (principal instanceof RolePrincipal rolePrincipal) {
            return Collections.singleton(rolePrincipal.getName());
        }
        return Collections.emptySet();
    }
}