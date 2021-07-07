package il.co.fbc.sizeoff.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Document
public class Job {
    @Id
    @NotNull
    private String name;
    @NotNull
    @Builder.Default
    private LocalDateTime stamp = LocalDateTime.now();
    private String error;
    private LocalDateTime errorDate;
}
