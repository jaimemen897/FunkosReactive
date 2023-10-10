package services.database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import routes.Routes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Getter
public class DataBaseManager {
    private static DataBaseManager instance;
    private final Routes routes = Routes.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DataBaseManager.class);
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private static final Lock lock = new ReentrantLock();
    private static boolean initDataBase = false;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;


    private DataBaseManager() {
        loadResources();

        connectionFactory = ConnectionFactories.get(dbUrl);

        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(20)
                .build();

        pool = new ConnectionPool(configuration);

        if (initDataBase) {
            startTables();
        }
    }


    public static synchronized DataBaseManager getInstance() {
        lock.lock();
        if (instance == null) {
            instance = new DataBaseManager();
        }
        lock.unlock();
        return instance;
    }

    private synchronized void loadResources() {
        logger.debug("Cargando propiedades de configuracion");
        try {
            InputStream dbProps = ClassLoader.getSystemResourceAsStream("database.properties");
            Properties properties = new Properties();
            properties.load(dbProps);
            dbUrl = properties.getProperty("db.url");
            dbUser = properties.getProperty("db.user");
            dbPassword = properties.getProperty("db.password");
            initDataBase = Boolean.parseBoolean(properties.getProperty("db.init"));

        } catch (IOException e) {
            logger.error("Error al leer el fichero de propiedades: " + e.getMessage());
        }
    }

    public synchronized void startTables() {
        logger.debug("Borrando tablas");
        executeScripts(routes.getRemoveSqlFile()).block();
        logger.debug("Creando tablas");
        executeScripts(routes.getCreateSqlFile()).block();
        logger.debug("Tablas creadas");
    }

    public Mono<Void> executeScripts(String script) {
        logger.debug("Ejecutando script: " + script);
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    String scriptContent;
                    try {
                        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(script)) {
                            if (inputStream == null) {
                                return Mono.error(new IOException("No se ha encontrado fichero script para inicializar tablas"));
                            } else {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    scriptContent = reader.lines().collect(Collectors.joining("\n"));
                                }
                            }
                        }
                        Statement statement = connection.createStatement(scriptContent);
                        return Mono.from(statement.execute());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                },
                Connection::close
        ).then();

    }

    public ConnectionPool getConnectionPool() {
        return this.pool;
    }

}
