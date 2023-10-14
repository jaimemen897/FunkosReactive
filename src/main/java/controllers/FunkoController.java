package controllers;

import enums.Modelo;
import enums.Tipo;
import exceptions.File.NotFoundFile;
import lombok.Getter;
import models.Funko;
import models.IdGenerator;
import models.Notificacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routes.Routes;
import services.funkos.FunkosNotifications;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class FunkoController {
    private static FunkoController instance;
    private final List<Funko> funkos = new ArrayList<>();
    private final FunkosNotifications notification;
    private final IdGenerator idGenerator;
    private final Routes routes;

    private FunkoController(FunkosNotifications notification) {
        this.notification = notification;
        idGenerator = IdGenerator.getInstance();
        routes = Routes.getInstance();
    }

    public static synchronized FunkoController getInstance(FunkosNotifications notification) {
        if (instance == null) {
            instance = new FunkoController(notification);
        }
        return instance;
    }

    public void loadCsv() {
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

                add(Funko.builder()
                        .cod(cod)
                        .id2(idGenerator.getAndIncrement())
                        .nombre(split[1])
                        .modelo(Modelo.valueOf(split[2]))
                        .precio(Double.parseDouble(split[3]))
                        .fechaLanzamiento(dia)
                        .build());

                line = br.readLine();
            }
        } catch (IOException e) {
            throw new NotFoundFile("No se ha encontrado el archivo");
        }
    }

    public void loadCsvWithOutNotify() {
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

                addWithoutNotify(Funko.builder()
                        .cod(cod)
                        .id2(idGenerator.getAndIncrement())
                        .nombre(split[1])
                        .modelo(Modelo.valueOf(split[2]))
                        .precio(Double.parseDouble(split[3]))
                        .fechaLanzamiento(dia)
                        .build());

                line = br.readLine();
            }
        } catch (IOException e) {
            throw new NotFoundFile("No se ha encontrado el archivo");
        }
    }

    public void addWithoutNotify(Funko funko) {
        funkos.add(funko);
    }

    public void add(Funko funko) {
        addWithoutNotify(funko);
        notification.notify(new Notificacion<>(Tipo.NEW, funko));
    }

    public void deleteWithoutNotify(Funko funko) {
        funkos.remove(funko);
    }

    public void delete(Funko funko) {
        deleteWithoutNotify(funko);
        notification.notify(new Notificacion<>(Tipo.DELETED, funko));
    }

    public Mono<Funko> expensiveFunko() {
        Funko funkoMono = funkos.stream().max(Comparator.comparingDouble(Funko::getPrecio)).orElse(null);
        return Mono.fromCallable(() -> funkoMono);
    }

    public Mono<Double> averagePrice() {
        double averagePrice = funkos.stream().mapToDouble(Funko::getPrecio).average().orElse(0.0);
        return Mono.fromCallable(() -> averagePrice);
    }

    public Mono<Map<Modelo, List<Funko>>> groupByModelo() {
        Map<Modelo, List<Funko>> map = funkos.stream().collect(Collectors.groupingBy(Funko::getModelo));
        return Mono.fromCallable(() -> map);
    }

    public Mono<Map<Modelo, Long>> funkosByModelo() {
        Map<Modelo, Long> map = funkos.stream().collect(Collectors.groupingBy(Funko::getModelo, Collectors.counting()));
        return Mono.fromCallable(() -> map);
    }

    public Flux<Funko> funkosIn2023() {
        List<Funko> funkosList = funkos.stream().filter(funko -> funko.getFechaLanzamiento().getYear() == 2023).toList();
        return Flux.fromIterable(funkosList);
    }

    public Mono<Double> numberStitch() {
        double number = funkos.stream().filter(funko -> funko.getNombre().contains("Stitch")).count();
        return Mono.fromCallable(() -> number);
    }

    public Flux<Funko> funkoStitch() {
        List<Funko> funkosList = funkos.stream().filter(funko -> funko.getNombre().contains("Stitch")).toList();
        return Flux.fromIterable(funkosList);
    }

}
