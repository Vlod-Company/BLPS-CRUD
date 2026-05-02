package ru.gigasigma.blpscrud.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.gigasigma.blpscrud.security.JwtAuthenticationToken;
import ru.gigasigma.blpscrud.service.CIDRService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IpWhiteListFilter extends OncePerRequestFilter {
    private final CIDRService cidrService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof UsernamePasswordAuthenticationToken) && !(auth instanceof JwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

        List<String> rolesStrings = roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        List<String> grantedRoles = cidrService.getValidRole(ipAddress, rolesStrings);

        log.info("For ip: {} granted roles {}", ipAddress, grantedRoles);

        List<SimpleGrantedAuthority> authorities = grantedRoles.stream().map(SimpleGrantedAuthority::new).toList();
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        newAuth.setDetails(auth.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        filterChain.doFilter(request, response);
    }
}
