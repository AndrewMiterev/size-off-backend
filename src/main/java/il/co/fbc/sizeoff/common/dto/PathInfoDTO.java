package il.co.fbc.sizeoff.common.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class PathInfoDTO {
    long number;
    long size;
    long numberFolder;
    long sizeFolder;

    public PathInfoDTO add(PathInfoDTO plus) {
        return PathInfoDTO.builder()
                .number(number + plus.number)
                .size(size + plus.size)
                .numberFolder(numberFolder + plus.numberFolder)
                .sizeFolder(sizeFolder + plus.sizeFolder)
                .build();
    }
}
