package il.co.fbc.sizeoff.interfaces;

import il.co.fbc.sizeoff.common.dto.ServerDTO;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;

import java.time.LocalDateTime;
import java.util.List;

public interface ServerInfoService {
    ServerInfoBo get(String serverName);
    void update(ServerInfoBo data);
    void delete(String serverName);
    void deleteOld(int hours);
    List<ServerDTO> getListOfAllServers();
    List<ServerDTO> getListOfOutdatedServers(int hours);
    LocalDateTime getLastUpdate();
}
