package il.co.fbc.sizeoff.services.bo;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ServerInfoBo {
    String name;

    String sqlHostName;
    String instanceId;
    String path;
    String backupDirectory;
    String reportDirectory;
    String tempDirectory;
    String serverUrl;
    String instanceDefaultDataPath;
    String instanceDefaultLogPath;

    String sqlVersion;
    String sqlVersionDescription;
    String mainCompany;
    String licenseFor;
    String priorityVersion;
    List<DatabaseInfoDTO> firms = new ArrayList<>();
    List<DatabaseInfoDTO> another = new ArrayList<>();

    Long numberFiles;
    Long filesSize;
    Long numberFolders;
    Long foldersSize;
    Long numberBackupFiles;
    Long backupSize;
    Long numberFilesCanClear;
    Long filesCanClearSize;

    Long freeSpacePriority;
    Long freeSpaceForMdf;
    Long freeSpaceForLdf;
    @Builder.Default
    LocalDateTime updated = LocalDateTime.now();
}
