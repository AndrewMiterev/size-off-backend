package il.co.fbc.sizeoff.services.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.co.fbc.sizeoff.controllers.dto.AuthenticationResponseDTO;
import il.co.fbc.sizeoff.controllers.dto.SocialResponseDTO;
import il.co.fbc.sizeoff.interfaces.UserService;
import il.co.fbc.sizeoff.services.bo.UserBo;
import il.co.fbc.sizeoff.services.configs.filter.JwtTokenAuthenticationFilter;
import il.co.fbc.sizeoff.services.configs.filter.JwtTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static il.co.fbc.sizeoff.common.Constants.*;

@Slf4j
@RequiredArgsConstructor
//@EnableWebSecurity(debug = true)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder encoder;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final ObjectMapper mapper;
    private final JwtTools jwtTools;

    @Bean
    public JwtTokenAuthenticationFilter jwtTokenVerifierFilter() {
        return new JwtTokenAuthenticationFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
//                .disable()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(jwtTokenVerifierFilter(), UsernamePasswordAuthenticationFilter.class)

//                 todo wtf ???  .headers().frameOptions().disable().and()
                .httpBasic().and()
                .authorizeRequests()

                .mvcMatchers(SWAGER_AUTH_WHITELIST).permitAll()
                .mvcMatchers(LOGIN_AUTH_WHITELIST).permitAll()
                .mvcMatchers(SOCIAL_LOGIN_AUTH_WHITELIST).permitAll()
                .and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .authorizationRequestRepository(new InMemoryRequestRepository())
                .and()
                .successHandler(this::successHandler)
//azure
                .and()
                .logout(logoutConfigurer -> logoutConfigurer.addLogoutHandler(this::logout)
                        .logoutSuccessHandler(this::onLogoutSuccess));
    }

    private void successHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final boolean create = request.getParameter("create").equalsIgnoreCase("true");
        final String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        final String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        final String name = ((OAuth2User) authentication.getPrincipal()).getAttribute("name");
        final String remoteAddress = ((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress();

        if (name == null || email == null) {
            log.warn("Social user name {} e-mail {} provider {} doesn't have valid name and e-mail", name, email, provider);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write(("Social user from provider (%s) doesn't have valid full name(%s) and e-mail(%s). " +
                    "Please fill in the following information inside the social provider: 'full name' and 'e-mail'. " +
                    "Or choose another social provider.").formatted(provider, name, email));
            return;
        }

        log.debug("Redirected social user create: {} provider: {} name: {} email: {} remote address: {}", create, provider, name, email, remoteAddress);

        if (create) {
            log.info("Create new social user {} {} from {}", name, email, provider);
            userService.registerUser(UserBo.builder()
                    .name(name)
                    .email(email)
                    .password(provider)
                    .build());
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            log.warn("Social user {} error {}", email, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(mapper.writeValueAsString(
                    SocialResponseDTO.builder()
                            .email(email)
                            .name(name)
                            .provider(provider)
                            .build()
            ));
            return;
        }
        List<GrantedAuthority> list = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        final Authentication usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email, null, list);
        userService.updateLoginTime(email);
        var token = jwtTools.authentication2Token(usernamePasswordAuthenticationToken, remoteAddress);
        AuthenticationResponseDTO responseDTO = AuthenticationResponseDTO.builder()
                .token(token)
                .build();
        response.getWriter().write(mapper.writeValueAsString(responseDTO));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void logout(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) {
        log.warn("logout");
        // todo token processing
        log.error("doesn't implemented {}", request.getHeader("Authorization"));
    }

    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                         Authentication authentication) throws IOException, ServletException {
        log.warn("onLogoutSuccess");
        // this code is just sending the 200 ok response and preventing redirect
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
