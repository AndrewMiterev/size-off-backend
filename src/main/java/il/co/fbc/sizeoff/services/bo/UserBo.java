package il.co.fbc.sizeoff.services.bo;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class UserBo {
    @Email
    @NotNull
    String email;
    String name;
    @NotBlank
    String password;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    String[] roles;
}
