package il.co.fbc.sizeoff.common.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DatabaseInfoDTO {
    @NotBlank
    String dataBaseName;
    String firmName;
    @Positive
    long mdfSize;
    @Positive
    long ldfSize;
}
