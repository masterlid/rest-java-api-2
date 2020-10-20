package tv.lid.cinema.api2;

import io.jooby.Jooby;
import io.jooby.MediaType;

import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.lid.cinema.api2.controllers.MovieController;
import tv.lid.cinema.api2.controllers.ScheduleController;
import tv.lid.cinema.api2.models.CommonModel;
import tv.lid.cinema.api2.models.MovieModel;
import tv.lid.cinema.api2.models.ScheduleModel;
import tv.lid.cinema.api2.storages.H2Storage;

public class App extends Jooby {
    private static final String CMD_OPERATE   = "operate",
                                CMD_INSTALL   = "install",
                                CMD_UNINSTALL = "uninstall";

    // инициализация класса
    {
        decoder(MediaType.json, (ctx, type) -> {
            try {
                return (new ObjectMapper())
                    .readValue(
                        ctx.body().bytes(),
                        Class.forName(type.getTypeName())
                    );
            } catch (ClassNotFoundException | JsonProcessingException exc) {
                return null;
            }
        });

        encoder(MediaType.json, (ctx, result) -> {
            ctx.setDefaultResponseType(MediaType.json);

            try {
                return (new ObjectMapper()).writeValueAsBytes(result);
            } catch (JsonProcessingException exc) {
                return null;
            }
        });

        path("/api2", () -> {
            // фильмы
            final MovieController movCtr = new MovieController();

            get("/movies",        movCtr.list);
            get("/movies/{page}", movCtr.list);
            post("/movie",        movCtr.create);
            get("/movie/{id}",    movCtr.find);
            put("/movie",         movCtr.modify);
            delete("/movie/{id}", movCtr.kill);

            // сеансы
            final ScheduleController schCtr = new ScheduleController();

            get("/schedules/{movieId}",        schCtr.list);
            get("/schedules/{movieId}/{page}", schCtr.list);
            post("/schedule",                  schCtr.create);
            get("/schedule/{id}",              schCtr.find);
            put("/schedule",                   schCtr.modify);
            delete("/schedule/{id}",           schCtr.kill);
        });
    }

    // создание таблиц в базе данных
    private static void install() throws SQLException {
        MovieModel.createTable();
        ScheduleModel.createTable();
    }

    // удаление таблиц из базы данных
    private static void uninstall() throws SQLException {
        ScheduleModel.dropTable();
        MovieModel.dropTable();
    }

    // нормальная работа приложения
    private static void operate(final String[] args) {
        runApp(args, App::new);
    }

    public static void main(final String[] args) {
        // создание соединения с БД и подключение
        final H2Storage h2s = new H2Storage();

        try {
            // подключение к серверу БД
            h2s.connect();

            // инициализация моделей
            CommonModel.initialize(h2s.dataSource());
        } catch (SQLException exc) {
            System.out.println("Unable to start H2 database server! Exiting...\n\n");
            return;
        }

        // хук завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // разрыв соединения с БД
                try {
                    h2s.disconnect();
                } catch (SQLException exc) {
                    System.out.println("Unable to stop H2 database server!\n\n");
                }
            }
        });

        // разбор командной строки
        try {
            if (args.length == 0 || args.length == 1) {
                if (args.length == 0 || args[0].equals(App.CMD_OPERATE)) { // обычный режим
                    App.operate(args);
                } else if (args[0].equals(App.CMD_INSTALL)) { // создание таблиц
                    App.install();
                } else if (args[0].equals(App.CMD_UNINSTALL)) { // удаление таблиц
                    App.uninstall();
                } else {
                    throw new Exception();
                }
            } else {
                throw new Exception();
            }
        } catch (SQLException exc) {
            System.out.println("SQL exception occured during the execution! Exiting...\n\n");
        } catch (Exception exc) {
            System.out.println("Incorrect command line arguments were specified! Exiting...\n\n");
        }
    }
}
