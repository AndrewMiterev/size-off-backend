package il.co.fbc.sizeoff.common.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
public class TabulaDTO {
    @NotNull(message = "must be")
    @Size(min = 6, message = "length must be minimum {min} symbols")
    @Size(max = 16, message = "length must be maximum {max} symbols")
    String name;
    @NotNull(message = "can't be null")
    @Size(min = 6, message = "can't be smaller then {min} symbols")
    @Size(max = 30, message = "can't be greater then {max} symbols")
    String host;                                                                //  Example:   IIS025\ESHBEL_PRIORITY
    @Size(max = 6, message = "not greater than {max} symbols")
//    @Pattern(regexp = "^[^#]*", message = "must not contain # symbol")
    @Pattern(regexp = "^[A-Za-z0-9#]*", message = "must not contain special symbols")
    String installationID;                                                      //  Example:   AAIB
    @NotNull(message = "can't be null")
    @Size(min = 14, message = "can't be smaller then {min} symbols")
    @Size(max = 40, message = "can't be greater then {max} symbols")
    @Pattern(regexp = "[a-zA-Z]:.*", message = "must contain disk letter: and path to priority directory")
    String path;                                                                //  Example:   D:\priority\bin.95
}
