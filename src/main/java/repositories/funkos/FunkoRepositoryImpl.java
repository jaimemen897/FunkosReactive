package repositories.funkos;

import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enums.Modelo;
import exceptions.File.ErrorInFile;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import models.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import services.database.DataBaseManager;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FunkoRepositoryImpl implements FunkoRepository {
    private static FunkoRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);
    private final ConnectionPool connectionFactory;

    private FunkoRepositoryImpl(DataBaseManager db) {
        this.connectionFactory = db.getConnectionPool();
    }

    public static FunkoRepositoryImpl getInstance(DataBaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositoryImpl(db);
        }
        return instance;
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Insertando funko: " + funko);
        String query = "INSERT INTO FUNKOS (cod, id2, nombre, modelo, precio, fechaLanzamiento) VALUES (?, ?, ?, ?, ?, ?)";

        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, funko.getCod().toString())
                        .bind(1, funko.getId2())
                        .bind(2, funko.getNombre())
                        .bind(3, funko.getModelo().toString())
                        .bind(4, funko.getPrecio())
                        .bind(5, funko.getFechaLanzamiento())
                        .execute()
                ).then(Mono.just(funko)),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        String query = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?, fechaLanzamiento = ? WHERE id2 = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, funko.getNombre())
                        .bind(1, funko.getModelo().toString())
                        .bind(2, funko.getPrecio())
                        .bind(3, funko.getFechaLanzamiento())
                        .bind(4, funko.getId2())
                        .execute()
                ).then(Mono.just(funko)),
                Connection::close
        );

    }

    @Override
    public Mono<Funko> findById(Long id) {
        logger.debug("Buscando funko por ID: " + id);
        String query = "SELECT * FROM FUNKOS WHERE id2 = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((fila, datos) ->
                        Funko.builder()
                                .id2(fila.get("id2", Long.class))
                                .cod(UUID.fromString(fila.get("cod", String.class)))
                                .nombre(fila.get("nombre", String.class))
                                .modelo(Modelo.valueOf(fila.get("modelo", String.class)))
                                .precio(fila.get("precio", Float.class).doubleValue())
                                .fechaLanzamiento(fila.get("fechaLanzamiento", java.time.LocalDate.class))
                                .build()
                ))),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        String query = "SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                        .execute()
                ).flatMap(result -> result.map((fila, datos) ->
                        Funko.builder()
                                .id2(fila.get("id2", Long.class))
                                .cod(UUID.fromString(fila.get("cod", String.class)))
                                .nombre(fila.get("nombre", String.class))
                                .modelo(Modelo.valueOf(fila.get("modelo", String.class)))
                                .precio(fila.get("precio", Float.class).doubleValue())
                                .fechaLanzamiento(fila.get("fechaLanzamiento", LocalDate.class))
                                .build()
                )),
                Connection::close
        );
    }

    @Override
    public Mono<Boolean> deleteById(Long idDelete) {
        logger.debug("Borrando funko por ID: " + idDelete);
        String query = "DELETE FROM FUNKOS WHERE id2 = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                                .bind(0, idDelete)
                                .execute()
                        ).flatMapMany(Result::getRowsUpdated)
                        .hasElements(),
                Connection::close
        );

    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Borrando todos los funkos");
        String query = "DELETE FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .execute()
                ).then(),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) {
        logger.debug("Buscando funko por nombre: " + nombre);
        String query = "SELECT * FROM FUNKOS WHERE nombre = ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(query)
                        .bind(0, "%" + nombre + "%")
                        .execute()
                ).flatMap(result -> result.map((fila, datos) ->
                        Funko.builder()
                                .id2(fila.get("id2", Long.class))
                                .cod(UUID.fromString(fila.get("cod", String.class)))
                                .nombre(fila.get("nombre", String.class))
                                .modelo(Modelo.valueOf(fila.get("modelo", String.class)))
                                .precio(fila.get("precio", Float.class).doubleValue())
                                .fechaLanzamiento(fila.get("fechaLanzamiento", java.time.LocalDate.class))
                                .build()
                )),
                Connection::close
        );
    }

    public Mono<Void> exportJson(String ruta) {
        logger.debug("Exportando funkos a JSON, ruta: " + ruta);

        return Mono.fromRunnable((() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Funko.class, new LocalDateAdapter());
            Gson gson = gsonBuilder.setPrettyPrinting().create();

            try (FileWriter writer = new FileWriter(ruta)) {
                gson.toJson(findAll().collectList(), writer);
            } catch (IOException e) {
                throw new ErrorInFile("Error al escribir en el archivo JSON: " + e.getMessage());
            }
        }));
    }
}
