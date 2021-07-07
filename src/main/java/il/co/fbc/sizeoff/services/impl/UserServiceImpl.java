package il.co.fbc.sizeoff.services.impl;

import il.co.fbc.sizeoff.common.exception.NonCriticException;
import il.co.fbc.sizeoff.domain.repo.UserRepository;
import il.co.fbc.sizeoff.interfaces.UserService;
import il.co.fbc.sizeoff.mapper.MapUserBo2UserEntity;
import il.co.fbc.sizeoff.mapper.MapUserEntity2UserBo;
import il.co.fbc.sizeoff.services.bo.UserBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    final private UserRepository repository;
    final private MapUserBo2UserEntity mapper2Entity;
    final private MapUserEntity2UserBo mapper2Bo;
    final private PasswordEncoder encoder;
    final private Environment environment;
    private final AtomicReference<LocalDateTime> stampUpdate = new AtomicReference<>();

    @PostConstruct
    private void init() {
        String adminLogin = environment.getProperty("sizeoff.admin-login");
        String adminName = environment.getProperty("sizeoff.admin-name");
        String adminPassword = environment.getProperty("sizeoff.admin-password");

        if (adminLogin != null && adminName != null && adminPassword != null)
            repository.save(mapper2Entity.map(UserBo.builder()
                    .email(adminLogin)
                    .name(adminName)
                    .password(encoder.encode(adminPassword))
                    .roles(new String[]{"ROLE_ADMIN"})
                    .build()));
    }

    @PostConstruct
    private void postConstructTimestamp() {
        var lastOperationDate = repository
                .findAll()
                .stream()
                .map(u->u.getLastLogin()!=null?u.getLastLogin():u.getRegistrationDate())
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        stampUpdate.lazySet(lastOperationDate);
    }

    private void updateStamp() {
        stampUpdate.lazySet(LocalDateTime.now());
    }

    @Override
    public LocalDateTime getLastUpdate() {
        return stampUpdate.get();
    }


    @Override
    public void updateLoginTime(String email) {
        var user = repository.findById(email)
                .orElseThrow(() -> new NonCriticException("User: %s doesn't exist".formatted(email)));
        user.setLastLogin(LocalDateTime.now());
        repository.save(user);
        updateStamp();
    }

    @Override
    public void registerUser(UserBo user) {
        if (repository.existsById(user.getEmail()))
            throw new NonCriticException("User: %s already exist!".formatted(user.getEmail()));
        user.setRegistrationDate(LocalDateTime.now());
        repository.insert(mapper2Entity.map(user));
        updateStamp();
    }

    @Override
    public void removeUser(String email) {
        if (!repository.existsById(email))
            throw new NonCriticException("User: %s doesn't exist".formatted(email));
        repository.deleteById(email);
        updateStamp();
    }

    @Override
    public void updateUser(UserBo user) {
        var oldUser = repository.findById(user.getEmail())
                .orElseThrow(() -> new NonCriticException("User: %s doesn't exist".formatted(user.getEmail())));
        var newUser = mapper2Entity.map(user);
        if (newUser.getPassword() == null) newUser.setPassword(oldUser.getPassword());
        repository.save(newUser);
        updateStamp();
    }

    @Override
    public Optional<UserBo> getUserInfo(String email) {
        return repository.findById(email).map(mapper2Bo::map);
    }

    @Override
    public List<UserBo> getAll() {
        return repository.findAll().stream().map(mapper2Bo::map).collect(Collectors.toList());
    }

    @Override
    public UserBo getCurrentPrincipal() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(p -> (p instanceof User) ? ((User) p).getUsername() : (String) p)
                .flatMap(this::getUserInfo)
                .orElse(null);
    }

    @Override
    public boolean thereIsAccessToTheServer(String serverName) {
        var principal = getCurrentPrincipal();
        if (principal == null) return false;
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER", "SERVER_%s".formatted(serverName));
//        log.debug("Roles {} server {} from roles {}",principal.getRoles(),serverName,roles);
        return Arrays.stream(principal.getRoles()).anyMatch(roles::contains);
    }
}
