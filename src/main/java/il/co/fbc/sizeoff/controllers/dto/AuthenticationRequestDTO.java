package il.co.fbc.sizeoff.controllers.dto;

import lombok.*;

import javax.validation.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuthenticationRequestDTO {
    @NotBlank
    @Email(message = "must be valid e-mail address")
    String email;
    @NotBlank
    @Size(min = 8, message = "at least minimum {min} characters")
    String password;
}
