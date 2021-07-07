package il.co.fbc.sizeoff.services.configs.filter;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTools {
    private final JwtConfig config;
    private final SecretKey secretKey;

    public String authentication2Token(Authentication authentication, String remoteAddress) {
        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("remoteIp", remoteAddress)
                .claim("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()))
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(config.getTokenExpirationInDays())))
                .signWith(secretKey)
                .compact();
        return "%s %s".formatted(config.getPrefix(), token);
    }

    public Authentication token2Authentication(String token, String remoteAddress) throws JwtException {
        if (token == null || !token.startsWith(config.getPrefix()))
            return null;
        token = token.replace(config.getPrefix(), "");
        var jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
        var claims = jwtParser.parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        if (username == null)
            throw new JwtException("username not specified");
        List<?> authorities = claims.get("authorities", List.class);
        if (authorities == null || authorities.size() < 1)
            throw new JwtException("roles not specified");
        String remoteIp = claims.get("remoteIp", String.class);
        if (remoteIp == null)
            throw new JwtException("token address not specified");
        if (!remoteIp.equalsIgnoreCase(remoteAddress))
            throw new JwtException("address from request %s, address from token %s".formatted(remoteAddress, remoteIp));
        List<GrantedAuthority> list = authorities.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(username, null, list);
    }
}
