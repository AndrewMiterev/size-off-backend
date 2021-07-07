package il.co.fbc.sizeoff.services.impl;

import il.co.fbc.sizeoff.common.dto.ServerDTO;
import il.co.fbc.sizeoff.domain.entities.Job;
import il.co.fbc.sizeoff.interfaces.JobService;
import il.co.fbc.sizeoff.interfaces.ScheduledRequesterService;
import il.co.fbc.sizeoff.interfaces.ServerInfoService;
import il.co.fbc.sizeoff.interfaces.UpdateServerInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;

import static il.co.fbc.sizeoff.common.Constants.*;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledRequesterServiceImpl implements ScheduledRequesterService {
    private final ServerInfoService serverInfoService;
    private final JobService jobService;
    private final UpdateServerInfoService updateServerInfoService;

    @Value("${sizeoff.refresh-rate-in-hours:24}")
    private int hoursToExpire;

    @Value("${sizeoff.scheduler-data-updater-delete-data-after-hours:2200}")
    private int clearDataAfterHours;
    @Value("${sizeoff.scheduler-clear-incomplete-jobs-in-hours:72}")
    private int clearJobsAfterHours;

    @Override
    public void setUpdateInterval(int hours) {
        log.info(String.format("Set update period from servers in %s hours", hours));
        hoursToExpire = hours;
    }

    @Override
    public int getUpdateInterval() {
        return hoursToExpire;
    }

    @PostConstruct
    void postConstructScheduledRequesterService() {
        if (hoursToExpire < MINIMUM_HOURS_TO_DATA_EXPIRE || hoursToExpire > MAXIMUM_HOURS_TO_DATA_EXPIRE) {
            String message = String.format("sizeoff.refresh-rate-in-hours not optimal! Recommendation MIN: %s MAX: %s OPTIMAL: %s. Now: %s"
                    , MINIMUM_HOURS_TO_DATA_EXPIRE, MAXIMUM_HOURS_TO_DATA_EXPIRE, OPTIMUM_HOURS_TO_DATA_EXPIRE, hoursToExpire);
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    @Scheduled(fixedDelayString = "${sizeoff.scheduler-data-updater-delay-in-milliseconds:3600000}"
            , initialDelayString = "${sizeoff.scheduler-data-updater-initial-delay-in-milliseconds:60000}")
//    @Scheduled(fixedDelay = 10000, initialDelay = 3000)
    private void scheduledJobUpdateRunner() {
        log.trace("Scheduler for update started");

        try {
            List<ServerDTO> listOutdatedInfo = serverInfoService.getListOfOutdatedServers(hoursToExpire);
            listOutdatedInfo.forEach(s -> {
                log.info("The information for server {} is outdated. Stamp: {}. I'll update it", s.getName(), s.getUpdated());
                jobService.add(s.getName());
            });
        } catch (RuntimeException e) {
            log.warn("Automatic job error. {}", e.getMessage());
        }

        List<Job> jobs = jobService.get();
        jobs.forEach(j -> {
            log.info("Found job for update server {} info", j.getName());
            updateServerInfoService.update(j.getName(),
                    s -> {
                        serverInfoService.update(s);
                        jobService.delete(j.getName());
                    }, e -> {
                        log.warn("Automatic job server {} error. {}", j.getName(), e.getMessage());
                        j.setError(e.getMessage());
                        j.setErrorDate(LocalDateTime.now());
                        jobService.update(j);
                    }
            );
        });
    }

    @Scheduled(fixedDelayString = "${sizeoff.scheduler-clear-delay-in-milliseconds:3600000}"
            , initialDelayString = "${sizeoff.scheduler-clear-initial-delay-in-milliseconds:60000}")
//    @Scheduled(fixedDelay = 10000, initialDelay = 3000)
    private void scheduledClearRunner() {
        log.trace("Scheduler for clear started");
        serverInfoService.deleteOld(clearDataAfterHours);
        jobService.deleteOld(clearJobsAfterHours);
    }
}
