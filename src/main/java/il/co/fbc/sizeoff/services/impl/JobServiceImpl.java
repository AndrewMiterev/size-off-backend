package il.co.fbc.sizeoff.services.impl;

import il.co.fbc.sizeoff.common.tools.NonCriticException;
import il.co.fbc.sizeoff.domain.entities.Job;
import il.co.fbc.sizeoff.domain.repo.JobRepository;
import il.co.fbc.sizeoff.interfaces.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

//todo del users without rights over 1 month
//todo del users without enters over 3 years

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {
    private final JobRepository repository;
    private final AtomicReference<LocalDateTime> stampUpdate = new AtomicReference<LocalDateTime>();

    @PostConstruct
    private void postConstruct() {
        var lastOperationDate = repository
                .findAll()
                .stream()
                .map(j -> j.getErrorDate() != null ? j.getErrorDate() : j.getStamp())
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
    public void add(String name) {
        name = name.toUpperCase();
        if (repository.existsById(name))
            throw new NonCriticException(String.format("Job for [%s] already exist", name));
        repository.save(Job.builder().name(name).build());
        updateStamp();
    }

    @Override
    public void delete(String name) {
        repository.deleteById(name.toUpperCase());
        updateStamp();
    }

    @Override
    public void update(Job job) {
        if (!repository.existsById(job.getName()))
            throw new NonCriticException(String.format("Job [%s] doesn't exist", job.getName()));
        repository.save(job);
        updateStamp();
    }

    @Override
    public List<Job> get() {
        return repository.findAll();
    }

    @Override
    public Map<String, Job> getMap() {
        return get().stream().collect(Collectors.toMap(Job::getName, j -> j));
    }

    @Override
    public Boolean exist(String name) {
        return repository.existsById(name.toUpperCase());
    }

    @Override
    public void deleteOld(int hoursToExpire) {
        repository.deleteAllByStampLessThan(LocalDateTime.now().minusHours(hoursToExpire));
        updateStamp();
    }

}
