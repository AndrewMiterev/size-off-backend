package il.co.fbc.sizeoff.common.dto;

import lombok.*;

@Builder
@ToString
@Getter
public class PriorityFoldersSizeDTO {
    long numberFiles;
    long filesSize;

    long numberFolders;
    long foldersSize;

    long numberBackupFiles;
    long backupSize;

    long numberFilesCanClear;
    long filesCanClearSize;

    long freeSpacePriority;
}
