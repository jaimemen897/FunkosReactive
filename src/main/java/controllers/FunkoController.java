package controllers;

import enums.Modelo;
import exceptions.File.NotFoundFile;
import lombok.Getter;
import models.Funko;
import models.IdGenerator;
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

    public CompletableFuture<List<Funko>> loadCsv() {
        return CompletableFuture.supplyAsync(() -> {
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

                    funkos.add(Funko.builder()
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
                throw new NotFoundFile("No se ha encontrado el archivo, " + e.getMessage());
            }
            return funkos;
        });
    }

    public CompletableFuture<Funko> expensiveFunko() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().max(Comparator.comparingDouble(Funko::getPrecio)).get());
    }

    public CompletableFuture<Double> averagePrice() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().mapToDouble(Funko::getPrecio).average().getAsDouble());
    }

    public CompletableFuture<Map<Modelo, List<Funko>>> groupByModelo() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().collect(Collectors.groupingBy(Funko::getModelo)));
    }

    public CompletableFuture<Map<Modelo, Long>> funkosByModelo() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().collect(Collectors.groupingBy(Funko::getModelo, Collectors.counting())));
    }

    public CompletableFuture<List<Funko>> funkosIn2023() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().filter(funko -> funko.getFechaLanzamiento().getYear() == 2023).collect(Collectors.toList()));
    }

    public CompletableFuture<Double> numberStitch() {
        return CompletableFuture.supplyAsync(() -> (double) funkos.stream().filter(funko -> funko.getNombre().contains("Stitch")).count());
    }

    public CompletableFuture<List<Funko>> funkoStitch() {
        return CompletableFuture.supplyAsync(() -> funkos.stream().filter(funko -> funko.getNombre().contains("Stitch")).collect(Collectors.toList()));
    }
}
