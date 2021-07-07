package il.co.fbc.sizeoff.controllers;

import il.co.fbc.sizeoff.common.dto.ServerDTO;
import il.co.fbc.sizeoff.controllers.dto.ServerInfoDTO;
import il.co.fbc.sizeoff.domain.entities.Job;
import il.co.fbc.sizeoff.interfaces.JobService;
import il.co.fbc.sizeoff.interfaces.UserService;
import il.co.fbc.sizeoff.mapper.MapServerInfoModel2DTO;
import il.co.fbc.sizeoff.interfaces.ServerInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static il.co.fbc.sizeoff.common.Constants.*;

@Slf4j
@RestController
@RequestMapping(INFO_PATH)
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class InfoController {
    private final MapServerInfoModel2DTO mapper;
    private final ServerInfoService service;
    private final UserService userService;
    private final JobService jobService;

    @PreAuthorize(LEVEL_OBSERVER)
    @GetMapping
    @Operation(summary = "Get information for server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ServerInfoDTO getInfo(
            @Size(min = 6, message = "length must be minimum {min} symbols")
            @Size(max = 20, message = "length must be maximum {max} symbols")
            @RequestParam(name = "server") String serverName
    ) {
        log.trace("Request info for server {}", serverName);
        if (!userService.thereIsAccessToTheServer(serverName))
            throw new AccessDeniedException("User %s is not authorized to access the server %s"
                    .formatted(userService.getCurrentPrincipal().getEmail(), serverName));
        return mapper.map(service.get(serverName));
    }

    @PreAuthorize(LEVEL_ADMIN)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete all server info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void deleteInfo(
            @Size(min = 6, message = "length must be minimum {min} symbols")
            @Size(max = 20, message = "length must be maximum {max} symbols")
            @RequestParam(name = "server") String serverName
    ) {
        log.trace("Delete info for server {}", serverName);
        service.delete(serverName);
    }

    @GetMapping(LIST_PATH)
    @PreAuthorize(LEVEL_OBSERVER)
    @Operation(summary = "Get list of Servers in service accessible to current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<ServerDTO> getListOfServers() {
//        log.trace("Request list of servers in storage accessible to user");
//        sleep("Request list", 3000);

        List<ServerDTO> allServers = service.getListOfAllServers();
        Map<String, Job> jobs = jobService.getMap();
        allServers.forEach(s -> {
            var j = jobs.get(s.getName());
            if (j != null) {
                s.setWillBeUpdated(true);
                s.setUpdateError(j.getError() != null);
            }
        });
        return allServers.stream()
                .filter(s -> userService.thereIsAccessToTheServer(s.getName()))
                .collect(Collectors.toList());
    }


    @GetMapping(TIMESTAMP_PATH)
    @PreAuthorize(LEVEL_OBSERVER)
    @Operation(summary = "Get timestamp of last change in servers infos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public LocalDateTime getTimeStamp() {
        log.trace("Request timestamp of servers infos");
        return service.getLastUpdate();
    }
}
