package il.co.fbc.sizeoff.services.configs;

import il.co.fbc.sizeoff.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticatorConfig implements UserDetailsService {
    private final UserService service;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        log.trace("user to authenticate {}", email);
        var user = service.getUserInfo(email)
                .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(email)));
//        log.debug("{} {} {}", user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRoles()));
        return new User(user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList(
                user.getRoles()));
    }
}
