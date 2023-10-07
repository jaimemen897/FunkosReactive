package repositories.funkos;

import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import repositories.crud.CrudRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface FunkoRepository extends CrudRepository<Funko, Long> {
    // Buscar por nombre
    CompletableFuture<Funko> save(Funko funko) throws SQLException;

    // Actualizar
    CompletableFuture<Funko> update(Funko funko) throws SQLException;

    // Borrar por ID
    CompletableFuture<Boolean> deleteById(Long id) throws SQLException, FunkoNotFoundException, ExecutionException, InterruptedException;

    // Borrar todos
    CompletableFuture<Void> deleteAll() throws SQLException;

    // Buscar por ID
    CompletableFuture<Optional<Funko>> findById(Long id) throws SQLException;

    // Buscar todos
    CompletableFuture<List<Funko>> findAll() throws SQLException;

    // Buscar por nombre
    CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException;
}
