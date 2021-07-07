package il.co.fbc.sizeoff.domain.repo;

import il.co.fbc.sizeoff.domain.entities.ServerInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ServerInfoRepository extends MongoRepository<ServerInfo, String> {
    List<ServerInfo> findTopByNameOrderByUpdatedDesc(String serverName);

    void deleteAllByUpdatedLessThan(LocalDateTime minusHours);

    void deleteAllByName(String serverName);
}
