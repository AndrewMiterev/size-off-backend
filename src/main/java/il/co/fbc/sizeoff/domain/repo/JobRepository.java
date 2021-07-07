package il.co.fbc.sizeoff.domain.repo;

import il.co.fbc.sizeoff.domain.entities.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface JobRepository extends MongoRepository <Job, String> {
    void deleteAllByStampLessThan(LocalDateTime minusHours);
}
