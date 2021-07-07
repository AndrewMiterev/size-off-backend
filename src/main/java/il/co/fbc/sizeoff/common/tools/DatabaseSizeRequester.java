package il.co.fbc.sizeoff.common.tools;

import il.co.fbc.sizeoff.common.dto.DatabaseInfoDTO;
import il.co.fbc.sizeoff.common.dto.DatabaseServerInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// for MS-SQL-auth please copy sqljdbc_auth.dll into dir: C:\Windows\System32

@Slf4j
public class DatabaseSizeRequester {
    private final String connectionString;
    private final String installationId;
    private final String sqlHostName;
    private final String serverName;

    public DatabaseSizeRequester(String serverName, String sqlHostName, String installationId, String login, String password) {
        this.serverName = serverName;
        this.installationId = installationId;
        this.sqlHostName = sqlHostName;
        this.connectionString = "%s%s".formatted(
                "jdbc:sqlserver://%s;databaseName=%ssystem;persistSecurityInfo=True;".formatted(sqlHostName, installationId),
                (login == null || password == null) ?
                        "integratedSecurity=True;"
                        : "user=%s;password=%s;".formatted(login, password)
        );
        log.debug(connectionString);
    }

    public String selectOneStringFromSql(Statement statement, String query) throws SQLException {
        ResultSet r = statement.executeQuery(query);
        return r.next() ? r.getString(1) : "";
    }

    public ResultSet selectDataFromSql(Statement statement, String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public DatabaseServerInfoDTO get() {
        log.trace("request to DB started {} {}", sqlHostName, installationId);
        try {
            DriverManager.registerDriver(new SQLServerDriver());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        try (
                Connection connection = DriverManager.getConnection(connectionString);
                Statement statement = connection.createStatement();
        ) {
            String sqlVersion = selectOneStringFromSql(statement, "select " +
                    "convert(varchar, SERVERPROPERTY ('edition'))+' '+" +
                    "convert(varchar, SERVERPROPERTY( 'productversion'))+' '+" +
                    "convert(varchar,SERVERPROPERTY ('productlevel'))");
            String sqlDescription = selectOneStringFromSql(statement, "select @@version")
                    .replace('\t', ' ')
                    .replace('\n', ' ')
                    .replaceAll("  +", " ");

            String nameMainCompany = selectOneStringFromSql(statement, "select TITLE from ENVIRONMENT where HRFLAG='Y'");

            String serverUrl = selectOneStringFromSql(statement, "select VALUE from NETDEFS where NAME='SERVERURL' and upper(SERVER)=upper('%s')".formatted(serverName));
            String instanceDefaultDataPath = selectOneStringFromSql(statement, "select convert(varchar, SERVERPROPERTY('InstanceDefaultDataPath'))");
            String instanceDefaultLogPath = selectOneStringFromSql(statement, "select convert(varchar, SERVERPROPERTY('InstanceDefaultLogPath'))");

//            log.info("{} {}", serverName, serverUrl);
//            log.info("select VALUE from NETDEFS where NAME='SERVERURL' and upper(SERVER)=upper('%s')".formatted(serverName));
//            if(2==2) throw new RuntimeException("Stop");

            // directory for disk free
            // select SERVERPROPERTY('InstanceDefaultDataPath')

            String licenseFor;
            try {
                // new priority license
                licenseFor = selectOneStringFromSql(statement, String.format("select value from %ssystem.dbo.t$prilicense where line=1", installationId));
            } catch (Exception ignored) {
                // old priority license
                licenseFor = selectOneStringFromSql(statement, String.format("select company from %ssystem.dbo.t$license", installationId));
            }
            String priorityVersion = selectOneStringFromSql(statement, String.format("select cast(a.VALUE as varchar) +'.'" +
                    "+cast(b.VALUE as varchar) from %ssystem.dbo.SYSCONST a, %ssystem.dbo.SYSCONST b " +
                    "where a.NAME like 'LASTUPG' and b.NAME='SERVICEPACK'", installationId, installationId));

            String query = String.format(
                    "with " +
                            "dbnames as (select name, dbid from sys.sysdatabases where len(sid)>1)," +
                            "dbfiles as (select name, dbid, fileid from sys.sysaltfiles)," +
                            "dbsizes as (select sum(cast(size as bigint)*8192) as size, dbid, groupid from sys.sysaltfiles group by dbid, groupid)," +
                            "mdfsize as (select size as mdf, dbid from dbsizes where groupid=1),ldfsize as (select size as ldf, dbid from dbsizes where groupid=0)," +
                            "priordb as (select DNAME as name, TITLE as firm from %ssystem.dbo.ENVIRONMENT where DNAME<>'' " +
                            "union all select 'system','Priority' union all select 'pritempdb', 'Priority')," +
                            "priorid as (select '%s'+name as name, firm from priordb)" +
                            "select dbnames.name, firm, mdf, ldf from dbnames " +
                            "join dbfiles on (dbnames.dbid=dbfiles.dbid)" +
                            "left join priorid on (dbnames.name=priorid.name)" +
                            "left join mdfsize on (dbnames.dbid=mdfsize.dbid)" +
                            "left join ldfsize on (dbnames.dbid=ldfsize.dbid)" +
                            "where fileid=1",
                    installationId, installationId);
            ResultSet r = selectDataFromSql(statement, query);
            List<DatabaseInfoDTO> list = new ArrayList<>();
            while (r.next()) {
                list.add(DatabaseInfoDTO.builder()
                        .dataBaseName(r.getString("name"))
                        .firmName(r.getString("firm"))
                        .mdfSize(r.getLong("mdf"))
                        .ldfSize(r.getLong("ldf"))
                        .build());
            }
            List<DatabaseInfoDTO> firms = list.stream().filter(d -> d.getFirmName() != null).collect(Collectors.toList());
            List<DatabaseInfoDTO> another = list.size() != firms.size() ? list.stream().filter(d -> d.getFirmName() == null).collect(Collectors.toList()) : null;

            Long[] freeSpace = new Long[2];
            query = String.format("select sys.sysaltfiles.groupid, available_bytes from sys.sysdatabases " +
                    "join sys.sysaltfiles on sys.sysaltfiles.dbid=sys.sysdatabases.dbid " +
                    "cross apply sys.dm_os_volume_stats(sys.sysdatabases.dbid, sys.sysaltfiles.groupid+1) " +
                    "where sys.sysdatabases.name like '%ssystem'", installationId);
            r = selectDataFromSql(statement, query);
            while (r.next())
                freeSpace[r.getInt(1)] = r.getLong(2);

            log.trace("request to DB finished {} {}", sqlHostName, installationId);
            return DatabaseServerInfoDTO.builder()
                    .firms(firms)
                    .another(another)
                    .licenseFor(licenseFor)
                    .mainCompany(nameMainCompany)
                    .serverUrl(serverUrl)
                    .instanceDefaultDataPath(instanceDefaultDataPath)
                    .instanceDefaultLogPath(instanceDefaultLogPath)
                    .priorityVersion(priorityVersion)
                    .sqlVersion(sqlVersion)
                    .sqlVersionDescription(sqlDescription)
                    .freeSpaceForMdf(freeSpace[0])
                    .freeSpaceForLdf(freeSpace[1])
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Database requester error. Server:%s. Installation ID:%s. %s".formatted(sqlHostName, installationId, e.getMessage()));
        }
    }
}
