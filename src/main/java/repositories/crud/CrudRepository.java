package repositories.crud;

import exceptions.Funko.FunkoNotFoundException;
import models.Funko;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface CrudRepository<T, ID> {
    // Guardar
    CompletableFuture<Funko> save(T t) throws SQLException;

    // Actualizar
    CompletableFuture<Funko> update(T t) throws SQLException, FunkoNotFoundException;

    // Buscar por ID
    CompletableFuture<Optional<Funko>> findById(ID id) throws SQLException;

    // Buscar por nombre
    CompletableFuture<List<Funko>> findByNombre(String nombre) throws SQLException;

    // Buscar todos
    CompletableFuture<List<Funko>> findAll() throws SQLException;

    // Borrar por ID
    CompletableFuture<Boolean> deleteById(ID id) throws SQLException, FunkoNotFoundException, ExecutionException, InterruptedException;

    // Borrar todos
    CompletableFuture<Void> deleteAll() throws SQLException;
}
