package services.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routes.Routes;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Getter
public class DataBaseManager implements AutoCloseable {
    private static DataBaseManager instance;
    private final Routes routes = Routes.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DataBaseManager.class);

    private HikariDataSource hikariDataSource;
    private static final Lock lock = new ReentrantLock();
    private static boolean initDataBase = false;

    private DataBaseManager() {
        openConnection();
    }


    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            lock.lock();
            initDataBase = true;
            instance = new DataBaseManager();
            lock.unlock();
        }
        return instance;
    }

    private synchronized void openConnection() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver: " + e.getMessage());
        }
        try {
            InputStream dbProps = ClassLoader.getSystemResourceAsStream("database.properties");
            Properties properties = new Properties();
            properties.load(dbProps);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));

            hikariDataSource = new HikariDataSource(config);

            Connection connection = hikariDataSource.getConnection();

            if (initDataBase) {
                Reader reader = new BufferedReader(new FileReader(routes.getRouteDirResources() + properties.getProperty("db.init")));
                ScriptRunner scriptRunner = new ScriptRunner(connection);
                scriptRunner.runScript(reader);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener la conexión: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error al leer el fichero de propiedades: " + e.getMessage());
        }
    }


    @Override
    public void close() {
        hikariDataSource.close();
    }


    public Connection getConnection() {
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Error al obtener la conexión: " + e.getMessage());
            return null;
        }
    }

}
