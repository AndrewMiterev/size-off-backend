package il.co.fbc.sizeoff.controllers.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class ErrorLineDTO {
    String rejectedField;
    String errorMessage;
    String rejectedValue;
}
