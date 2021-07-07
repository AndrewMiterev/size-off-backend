package il.co.fbc.sizeoff.services.bo;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TabulaBo {
    @NotNull(message = "can't be null")
    @Size(min = 6, message = "can't be smaller then {min} symbols")
    @Size(max = 30, message = "can't be greater then {max} symbols")
    String host;                                                                //  Example:   IIS025\ESHBEL_PRIORITY
    @Size(max = 6, message = "not greater than {max} symbols")
    String installationID;                                                      //  Example:   null or "" or AAIB or AAIB#
    @NotNull(message = "can't be null")
    @Size(min = 14, message = "can't be smaller then {min} symbols")
    @Size(max = 40, message = "can't be greater then {max} symbols")
    String path;                                                                //  Example:   \\servername\priorityPath
}
