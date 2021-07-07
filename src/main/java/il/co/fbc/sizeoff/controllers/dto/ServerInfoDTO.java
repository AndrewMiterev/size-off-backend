package il.co.fbc.sizeoff.controllers.dto;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ServerInfoDTO {
    String name;

    String sqlHostName;
    String instanceId;
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
    DatabaseInfoDTO systemDB;
    DatabaseInfoDTO pritempDB;

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

    LocalDateTime updated;
}
