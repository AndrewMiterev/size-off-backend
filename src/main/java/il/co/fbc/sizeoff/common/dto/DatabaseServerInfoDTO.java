package il.co.fbc.sizeoff.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@Getter
@Setter
public class DatabaseServerInfoDTO {
    String sqlVersion;
    String sqlVersionDescription;
    String mainCompany;
    String licenseFor;
    String priorityVersion;
    String serverUrl;
    String instanceDefaultDataPath;
    String instanceDefaultLogPath;

    @Builder.Default
    List<DatabaseInfoDTO> firms = new ArrayList<>();
    @Builder.Default
    List<DatabaseInfoDTO> another = new ArrayList<>();
    Long freeSpaceForMdf;
    Long freeSpaceForLdf;
}
