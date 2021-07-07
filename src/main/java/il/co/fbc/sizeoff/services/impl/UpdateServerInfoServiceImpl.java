package il.co.fbc.sizeoff.services.impl;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import il.co.fbc.sizeoff.common.tools.DatabaseSizeRequester;
import il.co.fbc.sizeoff.common.tools.DirectorySizeRequester;
import il.co.fbc.sizeoff.interfaces.UpdateServerInfoService;
import il.co.fbc.sizeoff.services.bo.ServerInfoBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateServerInfoServiceImpl implements UpdateServerInfoService {
    @Value("${sizeoff.file-size-max-for-reports:50000000}")
    private Long maximumFileSizeForReports;
    @Value("${sizeoff.file-size-max-for-temp:10000}")
    private Long maximumFileSizeForTemp;
    @Value("${sizeoff.sql-login:null}")
    private String loginForSql;
    @Value("${sizeoff.sql-password:null}")
    private String passwordForSql;

    @PostConstruct
    void testValidSqlLogin() {
        if (loginForSql.equals("null")) loginForSql = null;
        if (passwordForSql.equals("null")) passwordForSql = null;
        if (loginForSql == null ^ passwordForSql == null)
            throw new RuntimeException("properties parameters sizeoff.sql-login and sizeoff.sql-password must be together");
    }

    private String normalizeID(String id, String ch) {
        return id == null || id.length() == 0 ? "" : id.contains(ch) ? id : String.format("%s%s", id, ch);
    }

    private String normalizePath(String name, String path) {
        return Paths.get(String.format("\\\\%s\\%s\\..", name, path.replace(":", "$"))).normalize().toString();
    }

    private ServerInfoBo tabulaIniReaderAndParser(final ServerInfoBo si) {
        log.trace("serverName {}", si.getName());
        final String tabulaIniPath = "\\\\%s\\admin$\\tabula.ini".formatted(si.getName());
        try {
            final Wini ini = new Wini(new File(tabulaIniPath));
            si.setSqlHostName(ini.get("Environment", "Tabula Host"));
            String ID = ini.get("Environment", "Installation ID");
            if (ID == null) ID = ini.get("Environment", "Installation Id");
            si.setInstanceId(normalizeID(ID, "#"));
            String folder = ini.get("Environment", "Tabula Path");
            String path = normalizePath(si.getName(), folder);
            si.setPath(path);
            si.setBackupDirectory("%s\\backup".formatted(path));
            si.setReportDirectory("%s\\system\\reports".formatted(path));
            si.setTempDirectory("%s\\tmp".formatted(path));
        } catch (IOException ioException) {
            throw new RuntimeException("Can't read %s. %s".formatted(tabulaIniPath, ioException.getMessage()));
        }
        return si;
    }

    private long freeDiskSpace(final String path) {
        log.trace("start calculation free space for directory {}", path);
        long freePartitionSpace = new File(path).getFreeSpace();
        log.trace("finish calculation free space for directory {}", path);
        log.debug("free space for directory {} = {}", path, freePartitionSpace);
        return freePartitionSpace;
    }

    private ServerInfoBo readDatabaseSizeAndFillData(final ServerInfoBo si) {
        var dbServerInfo = new DatabaseSizeRequester(
                si.getName(), si.getSqlHostName(), si.getInstanceId(), loginForSql, passwordForSql)
                .get();
        si.setSqlVersion(dbServerInfo.getSqlVersion());
        si.setSqlVersionDescription(dbServerInfo.getSqlVersionDescription());
        si.setMainCompany(dbServerInfo.getMainCompany());
        si.setLicenseFor(dbServerInfo.getLicenseFor());
        si.setPriorityVersion(dbServerInfo.getPriorityVersion());
        si.setServerUrl(dbServerInfo.getServerUrl());
        si.setInstanceDefaultDataPath(dbServerInfo.getInstanceDefaultDataPath());
        si.setInstanceDefaultLogPath(dbServerInfo.getInstanceDefaultLogPath());
        List<DatabaseInfoDTO> firms = dbServerInfo.getFirms();
        if (firms != null) {
            si.setFirms(new ArrayList<DatabaseInfoDTO>(firms));
        }
        List<DatabaseInfoDTO> others = dbServerInfo.getAnother();
        if (others != null) {
            si.setAnother(new ArrayList<DatabaseInfoDTO>(others));
        }
        si.setFreeSpaceForMdf(dbServerInfo.getFreeSpaceForMdf());
        si.setFreeSpaceForLdf(dbServerInfo.getFreeSpaceForLdf());
        return si;
    }

    public static ForkJoinPool forkJoinPool = new ForkJoinPool(10);

    @Override
    public void update(final String server, Consumer<ServerInfoBo> onSuccess, Consumer<Throwable> onError) {

//        if (!(server.equalsIgnoreCase("PC-BAT7") || server.equalsIgnoreCase("PC-BAT8"))) return;

        var processingChain = CompletableFuture
                .supplyAsync(server::toUpperCase, forkJoinPool)
                .thenApplyAsync(n -> {
                    log.info("Start request information for server {}", n);
                    return n;
                }, forkJoinPool)
                .thenApplyAsync(n -> ServerInfoBo.builder().name(n).build(), forkJoinPool)
                .thenApplyAsync(this::tabulaIniReaderAndParser, forkJoinPool)
                .thenApplyAsync(this::readDatabaseSizeAndFillData, forkJoinPool)
                .thenApplyAsync(si -> {
                    si.setFreeSpacePriority(freeDiskSpace(si.getPath()));
                    return si;
                }, forkJoinPool)
                .thenApplyAsync(si -> {
                    var d = new DirectorySizeRequester(si.getPath()).getSize();
                    si.setNumberFiles(d.getNumber());
                    si.setFilesSize(d.getSize());
                    si.setNumberFolders(d.getNumberFolder());
                    si.setFoldersSize(d.getSizeFolder());
                    return si;
                }, forkJoinPool)
                .thenApplyAsync(si -> {
                    var d = new DirectorySizeRequester(si.getBackupDirectory()).getSize();
                    si.setNumberBackupFiles(d.getNumber() + d.getNumberFolder());
                    si.setBackupSize(d.getSize() + d.getSizeFolder());
                    return si;
                }, forkJoinPool)
                .thenApplyAsync(si -> {
                    var d = new DirectorySizeRequester(si.getReportDirectory(), maximumFileSizeForReports).getSize();
                    si.setNumberFilesCanClear(d.getNumber() + d.getNumberFolder());
                    si.setFilesCanClearSize(d.getSize() + d.getSizeFolder());
                    return si;
                }, forkJoinPool)
                .thenApplyAsync(si -> {
                    var d = new DirectorySizeRequester(si.getTempDirectory(), maximumFileSizeForTemp).getSize();
                    si.setNumberFilesCanClear(si.getNumberFilesCanClear() + d.getNumber() + d.getNumberFolder());
                    si.setFilesCanClearSize(si.getFilesCanClearSize() + d.getSize() + d.getSizeFolder());
                    return si;
                }, forkJoinPool)
                .whenCompleteAsync((d, e) -> {
                    if (d != null) {
                        log.info("Information for server {} ready. Write it", d.getName());
                        onSuccess.accept(d);
                    }
                    if (e != null) {
                        log.info("Information retrieval error. {}", e.getCause().getMessage());
                        onError.accept(e.getCause());
                    }
                }, forkJoinPool);

    }
}
