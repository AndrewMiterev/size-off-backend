package il.co.fbc.sizeoff.domain.repo;

import il.co.fbc.sizeoff.domain.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
