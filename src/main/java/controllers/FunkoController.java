package controllers;

import enums.Modelo;
import exceptions.File.NotFoundFile;
import lombok.Getter;
import models.Funko;
import models.IdGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routes.Routes;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Getter
public class FunkoController {
    private static FunkoController instance;
    private final List<Funko> funkos = new ArrayList<>();
    private final IdGenerator idGenerator = IdGenerator.getInstance();
    Routes routes = Routes.getInstance();
    private final static Lock lock = new ReentrantLock();

    public static FunkoController getInstance() {
        if (instance == null) {
            lock.lock();
            instance = new FunkoController();
            lock.unlock();
        }
        return instance;
    }

    public Flux<Funko> loadCsv() {
        return Flux.create(sink -> {
            try (BufferedReader br = new BufferedReader(new FileReader(routes.getRouteFunkosCsv()))) {
                String line = br.readLine();
                line = br.readLine();
                while (line != null) {
                    String[] split = line.split(",");

                    int year = Integer.parseInt(split[4].split("-")[0]);
                    int month = Integer.parseInt(split[4].split("-")[1]);
                    int day = Integer.parseInt(split[4].split("-")[2]);

                    LocalDate dia = LocalDate.of(year, month, day);

                    UUID cod = UUID.fromString(split[0].substring(0, 35));

                    Funko funko = Funko.builder()
                            .cod(cod)
                            .id2(idGenerator.getAndIncrement())
                            .nombre(split[1])
                            .modelo(Modelo.valueOf(split[2]))
                            .precio(Double.parseDouble(split[3]))
                            .fechaLanzamiento(dia)
                            .build();

                    sink.next(funko);
                    line = br.readLine();
                }
                sink.complete();
            } catch (IOException e) {
                sink.error(new NotFoundFile("No se ha encontrado el archivo, " + e.getMessage()));
            }
        });
    }

    public Mono<Funko> expensiveFunko() {
        return loadCsv()
                .collectList()
                .flatMap(funkoList -> {
                    if (funkoList.isEmpty()) {
                        return Mono.error(new NoSuchElementException("No hay funkos disponibles."));
                    }
                    return Mono.just(Collections.max(funkoList, Comparator.comparingDouble(Funko::getPrecio)));
                });
    }
    public Mono<Double> averagePrice() {
        return loadCsv()
                .map(Funko::getPrecio)
                .collect(Collectors.averagingDouble(Double::doubleValue))
                .defaultIfEmpty(0.0); // Manejar el caso cuando no hay funkos
    }

    public Mono<Map<Modelo, List<Funko>>> groupByModelo() {
        return loadCsv()
                .collect(Collectors.groupingBy(Funko::getModelo));
    }

    public Mono<Map<Modelo, Long>> funkosByModelo() {
        return loadCsv()
                .collect(Collectors.groupingBy(Funko::getModelo, Collectors.counting()));
    }

    public Mono<List<Funko>> funkosIn2023() {
        return loadCsv()
                .filter(funko -> funko.getFechaLanzamiento().getYear() == 2023)
                .collectList();
    }

    public Mono<Double> numberStitch() {
        return loadCsv()
                .filter(funko -> funko.getNombre().contains("Stitch"))
                .count()
                .map(Double::valueOf);
    }

    public Mono<List<Funko>> funkoStitch() {
        return loadCsv()
                .filter(funko -> funko.getNombre().contains("Stitch"))
                .collectList();
    }
}
