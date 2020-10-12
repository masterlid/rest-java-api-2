package tv.lid.cinema.api2.models;

import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.sql2o.Connection;

// класс модели кинофильма
public class MovieModel extends CommonModel {
    // имя SQL-таблицы с фильмами
    private static final String TABLE_MOVIES = "api2_movies";

    // название
    @JsonProperty(value = "title", required = true)
    public final String title;

    // длительность в минутах
    @JsonProperty(value = "duration", required = true)
    public final short duration;

    // год выхода
    @JsonProperty(value = "year", required = true)
    public final short year;

    // конструктор #1 -- используется для создания экземпляра из входящего запроса
    @JsonCreator
    public MovieModel(
        @JsonProperty("id")       final int    id,
        @JsonProperty("title")    final String title,
        @JsonProperty("duration") final short  duration,
        @JsonProperty("year")     final short  year
    ) {
        super(id);

        this.title    = title;
        this.duration = duration;
        this.year     = year;
    }

    // конструктор #2 -- используется для создания экземпляра с нуля
    public MovieModel(
        final String title,
        final short  duration,
        final short  year
    ) {
        this(0, title, duration, year);
    }

    // создание таблицы в БД
    public static void createTable() throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery(
                "CREATE TABLE IF NOT EXISTS " + MovieModel.TABLE_MOVIES + " (" +
                "id INT NOT NULL IDENTITY, " +
                "title VARCHAR(300) NOT NULL, " +
                "duration SMALLINT NOT NULL, " + 
                "year SMALLINT NOT NULL)"
            )
            .executeUpdate()
            .close();
    }

    // удаление таблицы из БД
    public static void dropTable() throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery("DROP TABLE IF EXISTS " + MovieModel.TABLE_MOVIES)
            .executeUpdate()
            .close();
    }

    // имя таблицы в БД
    public static String tableName() {
        return MovieModel.TABLE_MOVIES;
    }

    // подсчет количества записей в БД
    public static int count() throws SQLException {
        Connection con = CommonModel.sql2o.open();
        Integer    cnt = con.createQuery(
            "SELECT COUNT(*) FROM " + MovieModel.TABLE_MOVIES
        ).executeScalar(Integer.class);
        con.close();
        return cnt.intValue();
    }

    // проверка существования в БД записи с заданным идентификатором
    public static boolean exists(final int id) throws SQLException {
        Connection con = CommonModel.sql2o.open();
        Integer    cnt = con.createQuery(
            "SELECT COUNT(*) FROM " + MovieModel.TABLE_MOVIES + " WHERE id = :id"
        ).addParameter("id", id).executeScalar(Integer.class);
        con.close();
        return cnt.intValue() != 0;
    }

    // чтение записи из БД по заданному идентификатору
    public static MovieModel find(final int id) throws SQLException {
        Connection con    = CommonModel.sql2o.open();
        MovieModel result = con.createQuery(
            "SELECT id, title, duration, year FROM " + MovieModel.TABLE_MOVIES + " WHERE id = :id"
        ).addParameter("id", id).executeAndFetchFirst(MovieModel.class);
        con.close();
        return result;
    }

    // получить список записей из БД с постраничным выводом
    public static List<MovieModel> list(final int page, final int numb) throws SQLException {
        Connection       con    = CommonModel.sql2o.open();
        List<MovieModel> result = con.createQuery(
            "SELECT id, title, duration, year FROM " + MovieModel.TABLE_MOVIES + " ORDER BY year DESC LIMIT :limit OFFSET :offset"
        )
        .addParameter("limit",  numb)
        .addParameter("offset", (page - 1) * numb)
        .executeAndFetch(MovieModel.class);
        con.close();
        return result;
    }

    // удаление записи из БД по заданному идентификатору
    public static void kill(final int id) throws SQLException {
        CommonModel.sql2o
            .open()
            .createQuery("DELETE FROM " + MovieModel.TABLE_MOVIES + " WHERE id = :id")
            .addParameter("id", id)
            .executeUpdate()
            .close();
    }

    // сохранение данной записи в БД
    public void save() throws SQLException {
        Connection con = CommonModel.sql2o.open();
        if (this.id == 0) { // создание новой
            con.createQuery(
                "INSERT INTO " + MovieModel.TABLE_MOVIES +
                " (title, duration, year) VALUES (:title, :duration, :year)"
            )
            .addParameter("title",    this.title)
            .addParameter("duration", this.duration)
            .addParameter("year",     this.year)
            .executeUpdate();
        } else { // изменение ранее созданной
            con.createQuery(
                "UPDATE " + MovieModel.TABLE_MOVIES +
                " SET title = :title, duration = :duration, year = :year WHERE id = :id"
            )
            .addParameter("title",    this.title)
            .addParameter("duration", this.duration)
            .addParameter("year",     this.year)
            .addParameter("id",       this.id)
            .executeUpdate();
        };
        con.close();
    }
}
