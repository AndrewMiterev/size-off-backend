package il.co.fbc.sizeoff.common.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServerDTO {
    @NotNull
    private String name;
    private String licenseFor;
    private String mainCompany;
    private LocalDateTime updated;
    private Boolean willBeUpdated;
    private Boolean updateError;
}
