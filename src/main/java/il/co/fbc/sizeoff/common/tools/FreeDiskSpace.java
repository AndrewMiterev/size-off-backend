package il.co.fbc.sizeoff.common.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
public class FreeDiskSpace extends CompletableFuture<Long> {
    public FreeDiskSpace(String path) {
        Executors.newCachedThreadPool().submit(() -> {
            log.debug(String.format("start calculation free space for directory %s", path));
            long freePartitionSpace = new File(path).getFreeSpace();
            log.debug(String.format("finish calculation free space for directory %s = %s", path, freePartitionSpace));
            complete(freePartitionSpace);
        });
    }
}
