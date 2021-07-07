package il.co.fbc.sizeoff.interfaces;

import il.co.fbc.sizeoff.domain.entities.Job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface JobService {
    void add (String name);
    void delete (String name);
    void update(Job job);
    List<Job> get();
    Map<String, Job> getMap();
    Boolean exist (String name);
    void deleteOld(int hoursToExpire);
    LocalDateTime getLastUpdate();
}
