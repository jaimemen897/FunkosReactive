package services.funkos;

import exceptions.Funko.FunkoNotFoundException;
import exceptions.Funko.FunkoNotStoragedException;
import models.Funko;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface FunkosService {
    List<Funko> findAll() throws ExecutionException, InterruptedException;

    List<Funko> findByNombre(String nombre) throws ExecutionException, InterruptedException, FunkoNotFoundException;

    Optional<Funko> findById(long id) throws ExecutionException, InterruptedException, FunkoNotFoundException;

    Funko save(Funko alumno) throws ExecutionException, InterruptedException, FunkoNotStoragedException;

    Funko update(Funko alumno) throws ExecutionException, InterruptedException, FunkoNotStoragedException, FunkoNotFoundException;

    boolean deleteById(long id) throws ExecutionException, InterruptedException, FunkoNotFoundException;

    void deleteAll() throws ExecutionException, InterruptedException;
}
