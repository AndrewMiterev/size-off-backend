package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.domain.entities.ServerInfo;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapServerInfoBo2ServerInfo extends MapperFromTo<ServerInfoBo, ServerInfo> {
}
