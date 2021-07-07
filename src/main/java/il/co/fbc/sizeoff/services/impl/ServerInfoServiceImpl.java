package il.co.fbc.sizeoff.services.impl;

import il.co.fbc.sizeoff.common.dto.ServerDTO;
import il.co.fbc.sizeoff.domain.entities.ServerInfo;
import il.co.fbc.sizeoff.domain.repo.ServerInfoRepository;
import il.co.fbc.sizeoff.interfaces.ServerInfoRequesterService;
import il.co.fbc.sizeoff.interfaces.ServerInfoService;
import il.co.fbc.sizeoff.mapper.MapServerInfo2ServerInfoBo;
import il.co.fbc.sizeoff.mapper.MapServerInfoBo2ServerInfo;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerInfoServiceImpl implements ServerInfoService {
    private final ServerInfoRepository repository;
    private final MapServerInfoBo2ServerInfo mapper2Entity;
    private final MapServerInfo2ServerInfoBo mapper2Bo;
    private final MongoTemplate template;
    private final AtomicReference<LocalDateTime> stampUpdate = new AtomicReference<LocalDateTime>();

    @PostConstruct
    private void postConstruct() {
        var lastOperationDate = repository
                .findAll()
                .stream()
                .map(ServerInfo::getUpdated)
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
    public ServerInfoBo get(String serverName) {
        return repository
                .findTopByNameOrderByUpdatedDesc(serverName.toUpperCase())
                .stream()
                .map(mapper2Bo::map)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(ServerInfoBo data) {
        repository.save(mapper2Entity.map(data));
        updateStamp();
    }

    @Override
    public void delete(String serverName) {
        repository.deleteAllByName(serverName);
        updateStamp();
    }

    @Override
    public void deleteOld(int hours) {
//        log.trace(String.format("clear outdated data for %s hours",hours));
        repository.deleteAllByUpdatedLessThan(LocalDateTime.now().minusHours(hours));
        updateStamp();
    }

    @Override
    public List<ServerDTO> getListOfAllServers() {
        Aggregation aggWow = newAggregation(
                // select max from ... group by name + mainCompany + licenseFor
                group("name", "mainCompany", "licenseFor")
                        .max("updated").as("updated"),
                sort(Sort.Direction.DESC, "updated"),
                project("updated", "name", "mainCompany", "licenseFor"),
                // select only first result for company ordered by date desc
                group("name")
                        .max("updated").as("updated")
                        .first("mainCompany").as("mainCompany")
                        .first("licenseFor").as("licenseFor"),
                project("updated", "mainCompany", "licenseFor")
                        .and("name").previousOperation(),
                sort(Sort.Direction.DESC, "name")
        );
        AggregationResults<ServerDTO> results = template.aggregate(aggWow, "serverInfo", ServerDTO.class);
        return results.getMappedResults();
    }

    @Override
    public List<ServerDTO> getListOfOutdatedServers(int hours) {
        Aggregation agg = newAggregation(
//                project("name", "updated"),
                group("name").max("updated").as("updated"),
                project("updated").and("name").previousOperation(),
                match(Criteria.where("updated").lt(LocalDateTime.now().minusHours(hours)))
        );
        AggregationResults<ServerDTO> results = template.aggregate(agg, "serverInfo", ServerDTO.class);
        return results.getMappedResults();
    }
}
