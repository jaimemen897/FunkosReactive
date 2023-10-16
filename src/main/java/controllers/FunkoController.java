package controllers;

import enums.Modelo;
import exceptions.File.NotFoundFile;
import lombok.Getter;
import models.Funko;
import models.IdGenerator;
import reactor.core.publisher.Flux;
import routes.Routes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class FunkoController {
    private static FunkoController instance;
    private final IdGenerator idGenerator;
    private final Routes routes;

    private FunkoController() {
        idGenerator = IdGenerator.getInstance();
        routes = Routes.getInstance();
    }

    public static synchronized FunkoController getInstance() {
        if (instance == null) {
            instance = new FunkoController();
        }
        return instance;
    }

    public Flux<Funko> loadCsv() {
        return Flux.using(
                () -> new BufferedReader(new FileReader(routes.getRouteFunkosCsv())),
                br -> Flux.fromStream(br.lines().skip(1).map(line -> {
                    String[] split = line.split(",");

                    int year = Integer.parseInt(split[4].split("-")[0]);
                    int month = Integer.parseInt(split[4].split("-")[1]);
                    int day = Integer.parseInt(split[4].split("-")[2]);

                    LocalDate dia = LocalDate.of(year, month, day);
                    UUID cod = UUID.fromString(split[0].substring(0, 35));

                    return Funko.builder()
                            .cod(cod)
                            .id2(idGenerator.getAndIncrement())
                            .nombre(split[1]).modelo(Modelo.valueOf(split[2]))
                            .precio(Double.parseDouble(split[3]))
                            .fechaLanzamiento(dia)
                            .build();
                })),
                reader -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new NotFoundFile("No se ha encontrado el archivo");
                    }
                });
    }
}
