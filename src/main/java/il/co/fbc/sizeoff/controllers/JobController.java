package il.co.fbc.sizeoff.controllers;

import il.co.fbc.sizeoff.controllers.dto.JobDTO;
import il.co.fbc.sizeoff.interfaces.JobService;
import il.co.fbc.sizeoff.mapper.MapJob2JobDTO;
import il.co.fbc.sizeoff.mapper.MapJobDTO2Job;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

import static il.co.fbc.sizeoff.common.Constants.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(JOB_PATH)
@Slf4j
@RequiredArgsConstructor
// todo validator springfox-bean-validators https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
// todo swagger
// todo rewrite info controller and user controller by example of job controller

//@ApiResponses(value = {
//        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
//})
@Validated
public class JobController {
    private final JobService service;
    private final MapJob2JobDTO mapJob2JobDTO;
    private final MapJobDTO2Job mapJobDTO2Job;

    @PreAuthorize(LEVEL_USER)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add job for server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    void add(@Size(min = 6, message = "server name must be minimum {min} symbols")
             @Size(max = 20, message = "server name must be maximum {max} symbols")
             @RequestParam String name) {
        log.trace("add job for {}", name);
        service.add(name);
    }

    @PreAuthorize(LEVEL_USER)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete job for server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    void delete(@Size(min = 6, message = "server name must be minimum {min} symbols")
                @Size(max = 20, message = "server name must be maximum {max} symbols")
                @RequestParam String name) {
        log.trace("Delete job {}", name);
        service.delete(name);
    }

    @PreAuthorize(LEVEL_USER)
    @PutMapping
    @Operation(summary = "Update job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    void update(@Valid JobDTO job) {
        service.update(mapJobDTO2Job.map(job));
    }

    @PreAuthorize(LEVEL_USER)
    @GetMapping("list")
    @Operation(summary = "Get list of Jobs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<JobDTO> getList() {
        log.trace("Request list of jobs");
        return mapJob2JobDTO.map(service.get());
    }

    @PreAuthorize(LEVEL_USER)
    @GetMapping(TIMESTAMP_PATH)
    @Operation(summary = "Get timestamp of last change in jobs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public LocalDateTime getTimeStamp() {
        log.trace("Request timestamp of jobs");
        return service.getLastUpdate();
    }
}
