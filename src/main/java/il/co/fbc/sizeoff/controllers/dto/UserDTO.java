package il.co.fbc.sizeoff.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
    @Email
    @NotBlank
    String email;
    @NotBlank
    String name;
    @JsonIgnore
    String password;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private List<String> roles;
}
