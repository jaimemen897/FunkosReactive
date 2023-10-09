package services.funkos;

import enums.Tipo;
import exceptions.Funko.FunkoNotFoundException;
import exceptions.Funko.FunkoNotStoragedException;
import models.Funko;
import models.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repositories.funkos.FunkoRepositoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FunkosServiceImpl implements FunkosService {

    private static FunkosServiceImpl instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkoRepositoryImpl funkoRepository;
    private static final Lock lock = new ReentrantLock();
    private final FunkosNotifications notification;

    private FunkosServiceImpl(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
        this.funkoRepository = funkoRepository;
        this.cache = new FunkoCacheImpl();
        this.notification = notification;
    }

    public static FunkosServiceImpl getInstance(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
        if (instance == null) {
            lock.lock();
            instance = new FunkosServiceImpl(funkoRepository, notification);
            lock.unlock();
        }
        return instance;
    }


    @Override
    public Flux<Funko> findAll() throws ExecutionException, InterruptedException {
        return funkoRepository.findAll();
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) throws ExecutionException, InterruptedException, FunkoNotFoundException {
        Flux<Funko> funkos = funkoRepository.findByNombre(nombre);
        if (funkos == null) {
            throw new FunkoNotFoundException("No se ha encontrado ningún funko con el nombre: " + nombre);
        }
        return funkoRepository.findByNombre(nombre);
    }

    @Override
    public Mono<Funko> findById(long id) throws ExecutionException, InterruptedException, FunkoNotFoundException {
        Mono<Funko> funko = cache.get(id);
        if (funko != null) {
            logger.debug("Funko obtenido de la cache con id: " + id);
            return funko;
        } else {
            logger.debug("Funko obtenido de la base de datos con id: " + id);
            Mono<Funko> funkoDB = funkoRepository.findById(id);
            if (funkoDB == null) {
                throw new FunkoNotFoundException("No se ha encontrado ningún funko con el id: " + id);
            }
            return funkoRepository.findById(id);
        }
    }

    private Mono<Funko> saveWithNoNotifications(Funko funko) {
        logger.debug(("Guardando funko sin notificacion : " + funko));
        return funkoRepository.save(funko);
    }

    @Override
    public Mono<Funko> save(Funko funko) throws ExecutionException, InterruptedException, FunkoNotStoragedException {
        return saveWithNoNotifications(funko)
                .doOnSuccess(funko1 -> cache.put(funko1.getId2(), funko1));
    }

    private Mono<Funko> updateWithNoNotifications(Funko funko) {
        logger.debug("Actualizando funko sin notificacion: " + funko);
        return funkoRepository.findById(funko.getId2())
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("No se ha encontrado ningún funko con el id: " + funko.getId2()))
                        .flatMap(ifExist -> funkoRepository.update(funko)));
    }

    @Override
    public Mono<Funko> update(Funko funko) throws ExecutionException, InterruptedException, FunkoNotFoundException {
        logger.debug("Actualizando funko con id: " + funko.getId2());
        return updateWithNoNotifications(funko)
                .doOnSuccess(updated -> notification.notify(new Notificacion<>(Tipo.UPDATED, updated)));
    }

    private Mono<Funko> deleteByIdWithoutNotification(long id) {
        logger.debug("Borrando funko sin notificación con id: " + id);
        return funkoRepository.findById(id)
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado")))
                .flatMap(funko -> funkoRepository.deleteById(id).then(Mono.just(funko)));
    }

    @Override
    public Mono<Boolean> deleteById(long id) throws ExecutionException, InterruptedException, FunkoNotFoundException {
        logger.debug("Eliminando: " + id);
       return deleteByIdWithoutNotification(id)
               .doOnSuccess(deleted -> notification.notify(new Notificacion<>(Tipo.DELETED, deleted)))
               .map(funko -> true);
    }

    @Override
    public Mono<Void> deleteAll() throws ExecutionException, InterruptedException {
        logger.debug("Eliminando todos los funkos");
        cache.clear();
        return funkoRepository.deleteAll()
                .then(Mono.empty());
    }

    public Flux<Notificacion<Funko>> getNotifications(){
        return notification.getNotificationAsFlux();
    }
}
