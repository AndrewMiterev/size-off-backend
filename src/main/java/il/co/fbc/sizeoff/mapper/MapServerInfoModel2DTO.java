package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import il.co.fbc.sizeoff.controllers.dto.ServerInfoDTO;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;
import org.mapstruct.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MapServerInfoModel2DTO extends MapperFromTo<ServerInfoBo, ServerInfoDTO> {
    @Mapping(target = "systemDB", expression = "java(mapSystemDB(source,\"system\"))")
    @Mapping(target = "pritempDB", expression = "java(mapSystemDB(source,\"pritempdb\"))")
    @Mapping(target = "firms", expression = "java(firmsWithoutSystems(source))")
    ServerInfoDTO map(ServerInfoBo source);

    default DatabaseInfoDTO mapSystemDB(ServerInfoBo source, String name) {
        return source.getFirms()
                .stream()
                .filter(d -> d.getDataBaseName().toLowerCase().endsWith(name))
                .findFirst()
                .orElse(null);
    }

    default List<DatabaseInfoDTO> firmsWithoutSystems(ServerInfoBo source) {
        return source.getFirms()
                .stream()
                .filter(d -> !d.getDataBaseName().toLowerCase().endsWith("system")
                        && !d.getDataBaseName().toLowerCase().endsWith("pritempdb"))
                .collect(Collectors.toList());
    }
}
