package il.co.fbc.sizeoff.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorDTO<T> {
    int status;
    String error;
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
    List<ErrorLineDTO> errors;
    @JsonIgnore
    private T message;
}
