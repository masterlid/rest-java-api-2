package tv.lid.cinema.api2.storages;

import java.sql.SQLException;
import javax.sql.DataSource;

import org.h2.tools.Server;
import org.h2.jdbcx.JdbcDataSource;

// класс доступа к базе данных H2
public final class H2Storage extends DatabaseStorage {
    // дефолтные параметры подключения к базе данных
    private static final int    DEFAULT_DB_PORT     = 7799;        // дефолтный порт для подключения к серверу БД

    private static final String DEFAULT_DB_HOME     = "data",      // дефолтный каталог для хранения файла БД
                                DEFAULT_DB_FILE     = "cinema",    // дефолтный файл БД
                                DEFAULT_DB_USERNAME = "sa",        // дефолтное имя пользователя БД
                                DEFAULT_DB_PASSWORD = "sa@cinema"; // дефолтный пароль пользователя БД

    // параметры подключения к базе данных
    private final int    dbPort;
    private final String dbHome;
    private final String dbFile;
    private final String dbUsername;
    private final String dbPassword;

    // экземпляр сервера БД
    private Server srv = null;

    // data source
    private JdbcDataSource ds = null;

    // конструктор #1
    public H2Storage(
        int    dbPort,
        String dbHome,
        String dbFile,
        String dbUsername,
        String dbPassword
    ) {
        this.dbPort     = dbPort;
        this.dbHome     = dbHome;
        this.dbFile     = dbFile;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    // конструктор #2
    public H2Storage() {
        this(
            H2Storage.DEFAULT_DB_PORT,
            H2Storage.DEFAULT_DB_HOME,
            H2Storage.DEFAULT_DB_FILE,
            H2Storage.DEFAULT_DB_USERNAME,
            H2Storage.DEFAULT_DB_PASSWORD
        );
    }

    // установление соединения с базой данных
    public void connect() throws SQLException {
        try {
            // запуск сервера H2
            Class.forName("org.h2.Driver");
            this.srv = Server.createTcpServer(
                "-ifNotExists",
                "-tcpDaemon",
                "-tcpAllowOthers",
                "-tcpPort",
                String.valueOf(this.dbPort)
            ).start();

            // создание data source
            this.ds = new JdbcDataSource();
            this.ds.setURL(
                "jdbc:h2:tcp://localhost:" + this.dbPort + "/." + (this.dbHome.startsWith("/")
                    ? this.dbHome
                    : "/" + this.dbHome) + "/" + this.dbFile
            );
            this.ds.setUser(this.dbUsername);
            this.ds.setPassword(this.dbPassword);
        } catch (Exception exc) {
            throw new SQLException();
        }
    }

    // разрыв соединения с базой данных
    public void disconnect() throws SQLException {
        try {
            this.srv.stop();
        } catch (Exception exc) {
            throw new SQLException();
        }
    }

    // получить data source
    public DataSource dataSource() throws SQLException {
        if (this.ds != null) {
            return this.ds;
        } else {
            throw new SQLException();
        }
    }
}
