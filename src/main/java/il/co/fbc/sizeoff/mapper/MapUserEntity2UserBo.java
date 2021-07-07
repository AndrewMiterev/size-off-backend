package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.domain.entities.User;
import il.co.fbc.sizeoff.services.bo.UserBo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapUserEntity2UserBo extends MapperFromTo<User, UserBo> {
}
