package il.co.fbc.sizeoff.common.tools;

import il.co.fbc.sizeoff.common.dto.PathInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class DirectorySizeRequester {
    private final Path path;
    private final Long minSize;

    public DirectorySizeRequester(String directory, Long minSize) {
        this.path = Paths.get(directory).normalize();
        this.minSize = minSize;
    }

    public DirectorySizeRequester(String directory) {
        this(directory, 0L);
    }

    public PathInfoDTO getSize() {
        log.trace("start calculation size for directory: {}", path);
        PathInfoDTO resultSize = new PathInfoDTO();
        try {
            resultSize = Files
// ! 2,3, e.t.s. - for debug only. Example:  .walk(path, 2)
                    .walk(path)
                    .parallel()
                    .map(p -> {
                        try {
                            long fileSize = Files.size(p);
                            if (Files.isDirectory(p))
                                return PathInfoDTO.builder()
                                        .sizeFolder(fileSize)
                                        .numberFolder(1L)
                                        .build();
                            else return PathInfoDTO.builder()
                                    .size(fileSize > minSize ? fileSize : 0L)
                                    .number(fileSize > minSize ? 1L : 0L)
                                    .build();
                        } catch (IOException e) {
                            throw new RuntimeException("Trouble with get info file path %s %s".formatted(p, e.getMessage()));
                        }
                    })
                    .reduce(new PathInfoDTO(), PathInfoDTO::add);
        } catch (RuntimeException e) {
            log.warn("Calculation directory size {}. {}", path, e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchFileException e) {
            log.trace("Directory {} doesn't exists. {}", path, e.getMessage());
//            return resultSize;
        } catch (IOException e) {
            log.warn("IOException calculation directory {}. {}", path, e.getMessage());
            throw new RuntimeException(e);
        }
        log.trace("finish calculation size of directory {}", path);
        log.debug("size of directory: {} = {}", path, resultSize);
        return resultSize;
    }
}
