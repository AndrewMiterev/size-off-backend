package il.co.fbc.sizeoff.interfaces;

import il.co.fbc.sizeoff.services.bo.UserBo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    void updateLoginTime(String email);
    void registerUser(UserBo user);
    void removeUser(String email);
    Optional<UserBo> getUserInfo(String email);
    List<UserBo> getAll();
    UserBo getCurrentPrincipal();
    void updateUser(UserBo user);
    boolean thereIsAccessToTheServer(String serverName);
    LocalDateTime getLastUpdate();
}
