package il.co.fbc.sizeoff.controllers.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegistationRequestDTO {
    @NotBlank
    @Email(message = "must be valid e-mail address")
    String email;
    @NotBlank
    @Size(min = 5, message = "name and family. at least minimum {min} characters")
    @Size(max = 60, message = "name and family. maximum {max} characters")
    String name;
    @NotBlank
    @Size(min = 8, message = "at least minimum {min} characters")
    String password;
}
