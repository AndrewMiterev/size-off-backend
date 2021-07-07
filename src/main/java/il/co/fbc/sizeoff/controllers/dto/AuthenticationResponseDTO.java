package il.co.fbc.sizeoff.controllers.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuthenticationResponseDTO {
    String token;
}
