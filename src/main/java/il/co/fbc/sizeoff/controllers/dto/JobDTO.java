package il.co.fbc.sizeoff.controllers.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JobDTO {
    private String name;
    private LocalDateTime stamp;
    private String error;
    private LocalDateTime errorDate;
}
