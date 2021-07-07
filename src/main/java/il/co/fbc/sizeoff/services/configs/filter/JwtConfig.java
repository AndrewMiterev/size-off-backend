package il.co.fbc.sizeoff.services.configs.filter;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

@ConfigurationProperties("sizeoff.jwt")
@Component
@NoArgsConstructor
@Getter
@Setter
@ToString
@Validated
public class JwtConfig {
    @Size(min=40)
    String secret;
    String prefix;
    Integer tokenExpirationInDays;
}
