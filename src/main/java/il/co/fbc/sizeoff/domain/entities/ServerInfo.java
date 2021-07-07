package il.co.fbc.sizeoff.domain.entities;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ServerInfo {
    String name;

    String sqlVersion;
    String sqlVersionDescription;
    String mainCompany;
    String licenseFor;
    String priorityVersion;
    List<DatabaseInfoDTO> firms;
    List<DatabaseInfoDTO> another;
    String sqlHostName;
    String instanceId;
    String serverUrl;
    String instanceDefaultDataPath;
    String instanceDefaultLogPath;

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
