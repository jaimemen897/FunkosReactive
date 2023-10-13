package services.funkos;

import enums.Tipo;
import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import models.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import repositories.funkos.FunkoRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FunkosServiceImpl implements FunkosService {

    private static FunkosServiceImpl instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkoRepositoryImpl funkoRepository;
    private final FunkosNotifications notification;

    private FunkosServiceImpl(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
        this.funkoRepository = funkoRepository;
        this.cache = new FunkoCacheImpl();
        this.notification = FunkosNotificationsImpl.getInstance();
    }

    public synchronized static FunkosServiceImpl getInstance(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
        if (instance == null) {
            instance = new FunkosServiceImpl(funkoRepository, notification);
        }
        return instance;
    }

    @Override
    public Flux<Funko> findAll() {
        return funkoRepository.findAll();
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) throws FunkoNotFoundException {
        return funkoRepository.findByNombre(nombre)
                .flatMap(funko -> cache.put(funko.getId2(), funko)
                        .then(Mono.just(funko)))
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("No se ha encontrado ningún funko con el nombre: " + nombre)));

    }

    @Override
    public Mono<Funko> findById(long id) throws FunkoNotFoundException {
        return cache.get(id)
                .switchIfEmpty(funkoRepository.findById(id)
                        .flatMap(funko1 -> cache.put(funko1.getId2(), funko1)
                                .then(Mono.just(funko1)))
                        .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con ID: " + id + " no encontrado"))));
    }

    private Mono<Funko> saveWithNoNotifications(Funko funko) {
        logger.debug(("Guardando funko sin notificacion : " + funko));
        return funkoRepository.save(funko)
                .doOnSuccess(funko1 -> cache.put(funko1.getId2(), funko1));
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        return saveWithNoNotifications(funko)
        .doOnSuccess(saved -> notification.notify(new Notificacion<>(Tipo.NEW, saved)));
    }

    private Mono<Funko> updateWithNoNotifications(Funko funko) {
        logger.debug("Actualizando funko sin notificacion: " + funko);
        return funkoRepository.findById(funko.getId2())
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("No se ha encontrado ningún funko con el id: " + funko.getId2()))
                        .flatMap(ifExist -> funkoRepository.update(funko)));
    }

    @Override
    public Mono<Funko> update(Funko funko) {
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
    public Mono<Boolean> deleteById(long id) {
        logger.debug("Eliminando: " + id);
        return deleteByIdWithoutNotification(id)
                .doOnSuccess(deleted -> notification.notify(new Notificacion<>(Tipo.DELETED, deleted)))
                .map(funko -> true);
    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Eliminando todos los funkos");
        cache.clear();
        return funkoRepository.deleteAll()
                .then(Mono.empty());
    }

    public Flux<Notificacion<Funko>> getNotifications() {
        return notification.getNotificationAsFlux();
    }
}
