package services.funkos;

import controllers.FunkoController;
import enums.Modelo;
import enums.Tipo;
import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import models.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repositories.funkos.FunkoRepositoryImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunkosServiceImpl implements FunkosService {

    private static FunkosServiceImpl instance;
    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkoRepositoryImpl funkoRepository;
    private final FunkosNotifications notification;
    private final FunkoController funkoController = FunkoController.getInstance();


    private FunkosServiceImpl(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
        this.funkoRepository = funkoRepository;
        this.cache = new FunkoCacheImpl();
        this.notification = notification;

    }

    public static synchronized FunkosServiceImpl getInstance(FunkoRepositoryImpl funkoRepository, FunkosNotifications notification) {
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
    public Flux<Funko> findByNombre(String nombre) {
        return funkoRepository.findByNombre(nombre).flatMap(funko -> cache.put(funko.getId2(), funko).then(Mono.just(funko))).switchIfEmpty(Mono.error(new FunkoNotFoundException("No se ha encontrado ningún funko con el nombre: " + nombre)));
    }

    @Override
    public Mono<Funko> findById(long id) {
        return cache.get(id).switchIfEmpty(funkoRepository.findById(id).flatMap(funko1 -> cache.put(funko1.getId2(), funko1).then(Mono.just(funko1))).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con ID: " + id + " no encontrado"))));
    }

    public Mono<Funko> saveWithNoNotifications(Funko funko) {
        logger.debug(("Guardando funko sin notificacion : " + funko));
        return funkoRepository.save(funko).doOnSuccess(funko1 -> cache.put(funko1.getId2(), funko1));
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        return saveWithNoNotifications(funko).doOnSuccess(saved -> notification.notify(new Notificacion<>(Tipo.NEW, saved)));
    }

    public Mono<Funko> updateWithNoNotifications(Funko funko) {
        logger.debug("Actualizando funko sin notificacion: " + funko);

        return funkoRepository.findById(funko.getId2())
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + funko.getId2() + " no encontrado")))
                .flatMap(existing -> funkoRepository.update(funko)
                        .flatMap(updated -> cache.put(updated.getId2(), updated)
                                .thenReturn(updated)));
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko con id: " + funko.getId2());
        return updateWithNoNotifications(funko).doOnSuccess(updated -> notification.notify(new Notificacion<>(Tipo.UPDATED, updated)));
    }

    public Mono<Funko> deleteByIdWithoutNotification(long id) {
        logger.debug("Borrando funko sin notificación con id: " + id);
        return funkoRepository.findById(id).switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko con id " + id + " no encontrado"))).flatMap(funko -> funkoRepository.deleteById(id).then(Mono.just(funko)));
    }

    @Override
    public Mono<Boolean> deleteById(long id) {
        logger.debug("Eliminando: " + id);
        return deleteByIdWithoutNotification(id).doOnSuccess(deleted -> notification.notify(new Notificacion<>(Tipo.DELETED, deleted))).map(funko -> true);
    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Eliminando todos los funkos");
        cache.clear();
        return funkoRepository.deleteAll().then(Mono.empty());
    }

    public void exportToJson(String ruta) {
        funkoRepository.exportJson(ruta).subscribe();
    }

    public void importFromCsv() {
        deleteAll().subscribe();
        funkoController.loadCsv().subscribe(funko -> save(funko).subscribe());
    }

    public Mono<Funko> expensiveFunko() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().max(Comparator.comparingDouble(Funko::getPrecio)).orElse(null))
                .flatMap(Mono::justOrEmpty);
    }

    public Mono<Double> averagePrice() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().mapToDouble(Funko::getPrecio).average().orElse(0.0))
                .flatMap(Mono::justOrEmpty);
    }

    public Mono<Map<Modelo, List<Funko>>> groupByModelo() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().collect(Collectors.groupingBy(Funko::getModelo)))
                .flatMap(Mono::justOrEmpty);
    }

    public Mono<Map<Modelo, Long>> funkosByModelo() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().collect(Collectors.groupingBy(Funko::getModelo, Collectors.counting())))
                .flatMap(Mono::justOrEmpty);
    }

    public Flux<Funko> funkosIn2023() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().filter(funko -> funko.getFechaLanzamiento().getYear() == 2023).toList())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Double> numberStitch() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> (double) funkoList.stream().filter(funko -> funko.getNombre().contains("Stitch")).count())
                .flatMap(Mono::justOrEmpty);
    }

    public Flux<Funko> funkoStitch() {
        return findAll()
                .collectList()
                .mapNotNull(funkoList -> funkoList.stream().filter(funko -> funko.getNombre().contains("Stitch")).toList())
                .flatMapMany(Flux::fromIterable);
    }
}