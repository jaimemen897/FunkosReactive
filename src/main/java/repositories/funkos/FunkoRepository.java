package repositories.funkos;

import models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repositories.crud.CrudRepository;

import java.sql.SQLException;

public interface FunkoRepository extends CrudRepository<Funko, Long> {
    // Buscar por nombre
    Mono<Funko> save(Funko funko) throws SQLException;

    // Actualizar
    Mono<Funko> update(Funko funko) throws SQLException;

    // Buscar por ID
    Mono<Funko> findById(Long id) throws SQLException;

    // Buscar por nombre
    Flux<Funko> findByNombre(String nombre) throws SQLException;

    // Buscar todos
    Flux<Funko> findAll() throws SQLException;

    // Borrar por ID
    Mono<Boolean> deleteById(Long id);

    // Borrar todos
    Mono<Void> deleteAll() throws SQLException;
}
