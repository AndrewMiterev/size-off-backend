package il.co.fbc.sizeoff.mapper;

import il.co.fbc.sizeoff.controllers.dto.AuthenticationRequestDTO;
import il.co.fbc.sizeoff.controllers.dto.RegistationRequestDTO;
import il.co.fbc.sizeoff.controllers.dto.UserDTO;
import il.co.fbc.sizeoff.services.bo.UserBo;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class MapUserDTO2UserBo implements MapperFromTo<UserDTO, UserBo> {
    @Autowired
    PasswordEncoder encoder;

    public UserBo map(AuthenticationRequestDTO user) {
        return UserBo.builder()
                .password(encoder.encode(user.getPassword()))
                .email(user.getEmail())
                .build();
    }

    public UserBo map(RegistationRequestDTO user) {
        return UserBo.builder()
                .password(encoder.encode(user.getPassword()))
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
