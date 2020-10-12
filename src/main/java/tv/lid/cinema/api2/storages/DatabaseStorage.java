package tv.lid.cinema.api2.storages;

import java.sql.SQLException;
import javax.sql.DataSource;

// базовый абстрактный класс базы данных
public abstract class DatabaseStorage {
    // установление соединения с базой данных
    public abstract void connect() throws SQLException;

    // разрыв соединения с базой данных
    public abstract void disconnect() throws SQLException;

    // получить data source
    public abstract DataSource dataSource() throws SQLException;
}
