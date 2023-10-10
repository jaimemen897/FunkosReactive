package repositories.crud;

import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface CrudRepository<T, ID> {
    // Guardar
    Mono<T> save(T t) throws SQLException;

    // Actualizar
    Mono<T> update(T t) throws SQLException, FunkoNotFoundException;

    // Buscar por ID
    Mono<T> findById(ID id) throws SQLException;

    // Buscar por nombre
    Flux<T> findByNombre(String nombre) throws SQLException;

    // Buscar todos
    Flux<T> findAll() throws SQLException;

    // Borrar por ID
    Mono<Boolean> deleteById(ID id) throws SQLException, FunkoNotFoundException, ExecutionException, InterruptedException;

    // Borrar todos
    Mono<Void> deleteAll() throws SQLException;
}
