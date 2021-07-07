package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.common.dto.DatabaseServerInfoDTO;
import il.co.fbc.sizeoff.common.dto.PriorityFoldersSizeDTO;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapDirAndDB2ServerInfoBo {
    ServerInfoBo info2Bo(String name, PriorityFoldersSizeDTO s1, DatabaseServerInfoDTO s2);
}
