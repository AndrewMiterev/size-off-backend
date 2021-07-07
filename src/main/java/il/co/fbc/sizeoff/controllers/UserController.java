package il.co.fbc.sizeoff.controllers;

import il.co.fbc.sizeoff.controllers.dto.AuthenticationRequestDTO;
import il.co.fbc.sizeoff.controllers.dto.AuthenticationResponseDTO;
import il.co.fbc.sizeoff.controllers.dto.RegistationRequestDTO;
import il.co.fbc.sizeoff.controllers.dto.UserDTO;
import il.co.fbc.sizeoff.interfaces.UserService;
import il.co.fbc.sizeoff.mapper.MapUserBo2UserDTO;
import il.co.fbc.sizeoff.mapper.MapUserDTO2UserBo;
import il.co.fbc.sizeoff.services.configs.filter.JwtTools;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static il.co.fbc.sizeoff.common.Constants.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(AUTHENTICATION_PATH)
@CrossOrigin(origins = "*")
@Validated
public class UserController {
    private final UserService service;
    private final JwtTools jwtTools;
    private final AuthenticationManager authenticationManager;
    private final MapUserDTO2UserBo mapper2Bo;
    private final MapUserBo2UserDTO mapper2DTO;

    @PostMapping(LOGIN_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Login user. Return JWT block authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public AuthenticationResponseDTO loginUser(@Valid @RequestBody AuthenticationRequestDTO dto, HttpServletRequest request) {

//        sleep("login", 1000);
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        service.updateLoginTime(dto.getEmail());
        return AuthenticationResponseDTO.builder()
                .token(jwtTools.authentication2Token(authentication, request.getRemoteAddr()))
                .build();
    }

    @PreAuthorize(LEVEL_ANONYMOUS)
    @PostMapping(REGISTRATION_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void registerUser(@Valid @RequestBody RegistationRequestDTO user) {
//        sleep("register", 1000);
        service.registerUser(mapper2Bo.map(user));
    }

    private void sleep(String function, int delay) {
        log.debug("{} started", function);
        try {
            Thread.sleep(delay);
            log.debug("{} delayed {} ms", function, delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PreAuthorize(LEVEL_ADMIN)
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update old user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void updateUser(@Valid @RequestBody UserDTO user) {
//        sleep("update user", 3000);
        service.updateUser(mapper2Bo.map(user));
    }

    @PreAuthorize(LEVEL_ADMIN)
    @DeleteMapping("{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void deleteUser(@PathVariable @Email String email) {
//        sleep("delete user", 3000);
        service.removeUser(email);
    }

    @PreAuthorize(LEVEL_ADMIN)
    @GetMapping("one/{email}")
    @Operation(summary = "Get user information by email in path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public UserDTO getUserPath(@PathVariable @Email String email) {
//        sleep("get one", 1000);
        return mapper2DTO.map(service.getUserInfo(email).orElse(null));
    }

    @PreAuthorize(LEVEL_ADMIN)
    @GetMapping("list")
    @Operation(summary = "Get All users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<UserDTO> getAllUsers() {
//        sleep("users list", 3000);
        return service.getAll()
                .stream()
                .map(mapper2DTO::map)
                .collect(Collectors.toList());
    }

    @PreAuthorize(LEVEL_OBSERVER)
    @GetMapping("current")
    @Operation(summary = "Get current user information from Spring security context DEBUG")
    public String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().toString();
    }

    @PreAuthorize(LEVEL_OBSERVER)
    @GetMapping(TIMESTAMP_PATH)
    @Operation(summary = "Get last users update timestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public LocalDateTime getUsersTimestamp() {
        return service.getLastUpdate();
    }

}
