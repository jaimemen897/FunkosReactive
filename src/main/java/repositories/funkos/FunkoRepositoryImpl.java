package repositories.funkos;

import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enums.Modelo;
import exceptions.File.ErrorInFile;
import exceptions.BD.GetDataFromBD;
import exceptions.BD.InsertDataToBd;
import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import services.database.DataBaseManager;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FunkoRepositoryImpl implements FunkoRepository {
    private static FunkoRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);
    private final DataBaseManager db;
    private static final Lock lock = new ReentrantLock();

    private FunkoRepositoryImpl(DataBaseManager db) {
        this.db = db;
    }

    public static FunkoRepositoryImpl getInstance(DataBaseManager db) {
        if (instance == null) {
            lock.lock();
            instance = new FunkoRepositoryImpl(db);
            lock.unlock();
        }
        return instance;
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "INSERT INTO FUNKOS (cod, id2, nombre, modelo, precio, fechaLanzamiento) VALUES (?, ?, ?, ?, ?, ?)";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setString(1, funko.getCod().toString());
                stmt.setLong(2, funko.getId2());
                stmt.setString(3, funko.getNombre());
                stmt.setString(4, funko.getModelo().toString());
                stmt.setDouble(5, funko.getPrecio());
                stmt.setObject(6, funko.getFechaLanzamiento());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new InsertDataToBd("Error al insertar: " + e.getMessage());
            }
            logger.debug("Insertando funko: " + funko);
            return funko;
        });
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?, fechaLanzamiento = ? WHERE id2 = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setString(1, funko.getNombre());
                stmt.setString(2, funko.getModelo().toString());
                stmt.setDouble(3, funko.getPrecio());
                stmt.setDate(4, java.sql.Date.valueOf(funko.getFechaLanzamiento()));
                stmt.setLong(5, funko.getId2());
                var res = stmt.executeUpdate();
                if (res > 0) {
                    logger.debug("Funko actualizado");
                } else {
                    logger.error("No se ha podido guardar el funko");
                    throw new FunkoNotFoundException("No se ha podido guardar el funko");
                }
            } catch (SQLException | FunkoNotFoundException e) {
                logger.error(e.getMessage());
                throw new CompletionException(e);
            }
            return funko;
        });
    }

    @Override
    public Mono<Funko> findById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Funko> optionalFunko = Optional.empty();
            String query = "SELECT * FROM FUNKOS WHERE id2 = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, id);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    optionalFunko = Optional.of(Funko.builder()
                            .cod(UUID.fromString(rs.getString("cod")))
                            .id2(rs.getLong("id2"))
                            .nombre(rs.getString("nombre"))
                            .modelo(Modelo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fechaLanzamiento(rs.getDate("fechaLanzamiento").toLocalDate())
                            .build());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new GetDataFromBD("Error al encontrar por ID: " + e.getMessage());
            }
            return optionalFunko;
        });
    }

    @Override
    public Flux<Funko> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> lista = new ArrayList<>();
            String query = "SELECT * FROM FUNKOS";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    lista.add(Funko.builder()
                            .cod(UUID.fromString(rs.getString("cod")))
                            .id2(rs.getLong("id2"))
                            .nombre(rs.getString("nombre"))
                            .modelo(Modelo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fechaLanzamiento(rs.getDate("fechaLanzamiento").toLocalDate())
                            .build());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new GetDataFromBD("Error al encontrar todos: " + e.getMessage());
            }
            return lista;
        });
    }

    @Override
    public Mono<Funko> deleteById(Long idDelete) throws FunkoNotFoundException, ExecutionException, InterruptedException {
        Optional<Funko> funko = findById(idDelete).get();
        System.out.println(funko);
        if (funko.isPresent()) {
            throw new FunkoNotFoundException("No se ha encontrado ningún funko con el id: " + idDelete);
        }
        return CompletableFuture.supplyAsync(() -> {
            String query = "DELETE FROM FUNKOS WHERE id2 = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, idDelete);
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new InsertDataToBd("Error al eliminar por ID: " + e.getMessage());
            }
            return true;
        });

    }

    @Override
    public Mono<Void> deleteAll() {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM FUNKOS";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new InsertDataToBd("Error al eliminar todos: " + e.getMessage());
            }
        });
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> lista = new ArrayList<>();
            String query = "SELECT * FROM FUNKOS WHERE nombre = ?";
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setString(1, nombre);
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    lista.add(Funko.builder()
                            .cod(UUID.fromString(rs.getString("cod")))
                            .id2(rs.getLong("id2"))
                            .nombre(rs.getString("nombre"))
                            .modelo(Modelo.valueOf(rs.getString("modelo")))
                            .precio(rs.getDouble("precio"))
                            .fechaLanzamiento(rs.getDate("fechaLanzamiento").toLocalDate())
                            .build());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new GetDataFromBD("Error al encontrar por nombre: " + e.getMessage());
            }
            return lista;
        });
    }

    public CompletableFuture<Void> exportJson(String ruta) {
        return CompletableFuture.runAsync(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Funko.class, new LocalDateAdapter());
            Gson gson = gsonBuilder.setPrettyPrinting().create();

            try (FileWriter writer = new FileWriter(ruta)) {
                gson.toJson(findAll().get(), writer);
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new ErrorInFile("Error al escribir en el archivo JSON: " + e.getMessage());
            }
        });
    }
}
