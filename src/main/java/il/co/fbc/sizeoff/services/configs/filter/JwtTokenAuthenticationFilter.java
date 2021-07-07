package il.co.fbc.sizeoff.services.configs.filter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTools jwtTools;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            Authentication authentication = jwtTools.token2Authentication(authorizationHeader, request.getRemoteAddr());
            if (authentication == null) return;
            SecurityContextHolder.getContext().setAuthentication(authentication);
//            log.debug("User from token {}", authentication);
        } catch (JwtException e) {
            log.warn("Token: cannot be trusted. Trouble: {}. Token: {}", e.getMessage(), authorizationHeader);
        } finally {
            chain.doFilter(request, response);
        }
    }
}