package ru.gigasigma.blpscrud.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.gigasigma.blpscrud.service.CIDRService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IpWhiteListFilter extends OncePerRequestFilter {
    private final String proxyIp;

    private final CIDRService cidrService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();

        if (ipAddress.equals(proxyIp)) {
            if (request.getHeader("X-Forwarded-For") == null) {
                filterChain.doFilter(request, response);
                return;
            }
            ipAddress = request.getHeader("X-Forwarded-For");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> rolesList = auth.getAuthorities();

        List<String> rolesStrings = rolesList.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        List<String> newRoles = cidrService.getValidRole(ipAddress, rolesStrings);

        log.info("For ip: {} got: {} and granted roles: {}", ipAddress, rolesStrings, newRoles);

        List<SimpleGrantedAuthority> authorities = newRoles.stream().map(SimpleGrantedAuthority::new).toList();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        filterChain.doFilter(request, response);
    }
}
