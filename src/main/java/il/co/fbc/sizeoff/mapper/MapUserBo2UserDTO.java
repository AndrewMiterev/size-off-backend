package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.controllers.dto.UserDTO;
import il.co.fbc.sizeoff.services.bo.UserBo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapUserBo2UserDTO extends MapperFromTo<UserBo, UserDTO> {
}
