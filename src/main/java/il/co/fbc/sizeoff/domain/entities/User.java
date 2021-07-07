package il.co.fbc.sizeoff.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Document
public class User {
    @Id
    @Email
    private String email;
    @NotNull
    private String name;
    @NotNull
    private String password;
    @NotNull
    @Builder.Default
    private LocalDateTime registrationDate = LocalDateTime.now();
    private LocalDateTime lastLogin;
    @NotNull
    @Builder.Default
    private List<String> roles = new ArrayList<>();
}
