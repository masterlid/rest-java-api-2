package tv.lid.cinema.api2.models;

import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.sql2o.Connection;

// класс модели киносеанса
@JsonIgnoreProperties(value = "movieId", allowSetters = true)
public class ScheduleModel extends CommonModel {
    // имя SQL-таблицы с сеансами
    private static final String TABLE_SCHEDULES = "api2_schedules";

    // идентификатор фильма
    @JsonProperty(value = "movieId", required = true)
    public final int movieId;

    // дата и время начала
    @JsonProperty(value = "dateAndTime", required = true)
    public final String dateAndTime;

    // номер зала
    @JsonProperty(value = "auditorium", required = false, defaultValue = "1")
    public final byte auditorium;

    // конструктор #1 -- используется для создания экземпляра из входящего запроса
    @JsonCreator
    public ScheduleModel(
        @JsonProperty("id")          final int    id,
        @JsonProperty("movieId")     final int    movieId,
        @JsonProperty("dateAndTime") final String dateAndTime,
        @JsonProperty("auditorium")  final byte   auditorium
    ) {
        super(id);

        this.movieId     = movieId;
        this.dateAndTime = dateAndTime;
        this.auditorium  = auditorium;
    }

    // конструктор #2 -- используется для создания экземпляра с нуля
    public ScheduleModel(
        final int    movieId,
        final String dateAndTime,
        final byte   auditorium
    ) {
        this(0, movieId, dateAndTime, auditorium);
    }

    // создание таблицы в БД
    public static void createTable() throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery(
                "CREATE TABLE IF NOT EXISTS " + ScheduleModel.TABLE_SCHEDULES + " (" +
                "id INT NOT NULL IDENTITY, " +
                "movie_id INT NOT NULL, " + 
                "date_time VARCHAR(50) NOT NULL, " +
                "auditorium TINYINT NOT NULL, " +
                "FOREIGN KEY(movie_id) REFERENCES " + MovieModel.tableName() + "(id) ON DELETE CASCADE)"
            )
            .executeUpdate()
            .close();
    }

    // удаление таблицы из БД
    public static void dropTable() throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery("DROP TABLE IF EXISTS " + ScheduleModel.TABLE_SCHEDULES)
            .executeUpdate()
            .close();
    }

    // имя таблицы в БД
    public static String tableName() {
        return ScheduleModel.TABLE_SCHEDULES;
    }

    // подсчет количества записей в БД по заданному идентификатору фильма
    public static int count(final int movieId) throws SQLException {
        Connection con = CommonModel.sql2o.open();
        Integer    cnt = con.createQuery(
            "SELECT COUNT(*) FROM " + ScheduleModel.TABLE_SCHEDULES + " WHERE movie_id = :movie_id"
        ).addParameter("movie_id", movieId).executeScalar(Integer.class);
        con.close();
        return cnt.intValue();
    }

    // проверка существования в БД записи с заданным идентификатором
    public static boolean exists(final int id) throws SQLException {
        Connection con = CommonModel.sql2o.open();
        Integer    cnt = con.createQuery(
            "SELECT COUNT(*) FROM " + ScheduleModel.TABLE_SCHEDULES + " WHERE id = :id"
        ).addParameter("id", id).executeScalar(Integer.class);
        con.close();
        return cnt.intValue() != 0;
    }

    // чтение записи из БД по заданному идентификатору
    public static ScheduleModel find(final int id) throws SQLException {
        Connection    con    = CommonModel.sql2o.open();
        ScheduleModel result = con.createQuery(
            "SELECT id, movie_id AS movieId, date_time AS dateAndTime, auditorium FROM " +
            ScheduleModel.TABLE_SCHEDULES + " WHERE id = :id"
        ).addParameter("id", id).executeAndFetchFirst(ScheduleModel.class);
        con.close();
        return result;
    }

    // получить список записей из БД в соответствии с заданными параметрами
    public static List<ScheduleModel> list(
        final int movieId,
        final int page,
        final int numb
    ) throws SQLException {
        Connection          con    = CommonModel.sql2o.open();
        List<ScheduleModel> result = con.createQuery(
            "SELECT id, movie_id AS movieId, date_time AS dateAndTime, auditorium FROM " +
            ScheduleModel.TABLE_SCHEDULES + " WHERE movie_id = :movie_id ORDER BY dateAndTime DESC LIMIT :limit OFFSET :offset"
        )
        .addParameter("movie_id", movieId)
        .addParameter("limit",    numb)
        .addParameter("offset",   (page - 1) * numb)
        .executeAndFetch(ScheduleModel.class);
        con.close();
        return result;
    }

    // удаление записи из БД по заданному идентификатору
    public static void kill(final int id) throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery("DELETE FROM " + ScheduleModel.TABLE_SCHEDULES + " WHERE id = :id")
            .addParameter("id", id)
            .executeUpdate()
            .close();
    }

    // сохранение данной записи в БД
    public void save() throws SQLException {
        Connection con = CommonModel.sql2o.open();
        if (this.id == 0) { // создание новой
            con.createQuery(
                "INSERT INTO " + ScheduleModel.TABLE_SCHEDULES +
                " (movie_id, date_time, auditorium) VALUES (:movie_id, :date_time, :auditorium)"
            )
            .addParameter("movie_id",   this.movieId)
            .addParameter("date_time",  this.dateAndTime)
            .addParameter("auditorium", this.auditorium)
            .executeUpdate();
        } else { // изменение ранее созданной
            con.createQuery(
                "UPDATE " + ScheduleModel.TABLE_SCHEDULES +
                " SET movie_id = :movie_id, date_time = :date_time, auditorium = :auditorium WHERE id = :id"
            )
            .addParameter("movie_id",   this.movieId)
            .addParameter("date_time",  this.dateAndTime)
            .addParameter("auditorium", this.auditorium)
            .addParameter("id", id)
            .executeUpdate();
        };
        con.close();
    }
}
