package services.funkos;

import enums.Modelo;
import models.Funko;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repositories.funkos.FunkoRepositoryImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunkosServiceImplTest {
    @Mock
    FunkoRepositoryImpl repository;

    @Mock
    FunkosNotifications notifications;

    @InjectMocks
    FunkosServiceImpl service;

    @Test
    void findAll() throws SQLException {
        var funkos = List.of(
                Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build(),
                Funko.builder().cod(UUID.randomUUID()).id2(2L).nombre("Mate").modelo(Modelo.DISNEY).precio(90.0).fechaLanzamiento(LocalDate.parse("2023-10-07")).build()
        );

        when(repository.findAll()).thenReturn(Flux.fromIterable(funkos));
        var result = service.findAll().collectList().block();

        assertAll("findAll",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Rayo McQueen", result.get(0).getNombre()),
                () -> assertEquals("Mate", result.get(1).getNombre()),
                () -> assertEquals(100.0, result.get(0).getPrecio()),
                () -> assertEquals(90.0, result.get(1).getPrecio()),
                () -> assertEquals(LocalDate.parse("2021-10-07"), result.get(0).getFechaLanzamiento()),
                () -> assertEquals(LocalDate.parse("2023-10-07"), result.get(1).getFechaLanzamiento()),
                () -> assertEquals(Modelo.DISNEY, result.get(0).getModelo()),
                () -> assertEquals(Modelo.DISNEY, result.get(1).getModelo()),
                () -> assertEquals(1L, result.get(0).getId2()),
                () -> assertEquals(2L, result.get(1).getId2()),
                () -> assertNotNull(result.get(0).getCod()),
                () -> assertNotNull(result.get(1).getCod())
        );

        verify(repository, times(1)).findAll();
    }

    @Test
    void findByNombre() {
        var funkos = List.of(Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build());

        when(repository.findByNombre("Rayo McQueen")).thenReturn(Flux.fromIterable(funkos));
        var result = service.findByNombre("Rayo McQueen").collectList().block();

        assertAll("findByNombre",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Rayo McQueen", result.get(0).getNombre()),
                () -> assertEquals(100.0, result.get(0).getPrecio()),
                () -> assertEquals(LocalDate.parse("2021-10-07"), result.get(0).getFechaLanzamiento()),
                () -> assertEquals(Modelo.DISNEY, result.get(0).getModelo()),
                () -> assertEquals(1L, result.get(0).getId2()),
                () -> assertNotNull(result.get(0).getCod())
        );

        verify(repository, times(1)).findByNombre("Rayo McQueen");
    }

    @Test
    void findById() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.findById(1L)).thenReturn(Mono.just(funko));
        var result = service.findById(1L).blockOptional();

        assertAll("findById",
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("Rayo McQueen", result.get().getNombre()),
                () -> assertEquals(100.0, result.get().getPrecio()),
                () -> assertEquals(LocalDate.parse("2021-10-07"), result.get().getFechaLanzamiento()),
                () -> assertEquals(Modelo.DISNEY, result.get().getModelo()),
                () -> assertEquals(1L, result.get().getId2()),
                () -> assertNotNull(result.get().getCod())
        );

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExiste() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.findById(1L)).thenReturn(Mono.empty());

        var result = assertThrows(Exception.class, () -> service.findById(1L).blockOptional());
        System.out.println(result.getMessage());
        assertTrue(result.getMessage().contains("exceptions.Funko.FunkoNotFoundException: Funko con ID: 1 no encontrado"));

        verify(repository, times(1)).findById(1L);
    }


    @Test
    void save() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.save(funko)).thenReturn(Mono.just(funko));

        var result = service.saveWithNoNotifications(funko).block();

        assertAll("save",
                () -> assertNotNull(result),
                () -> assertEquals("Rayo McQueen", result.getNombre()),
                () -> assertEquals(100.0, result.getPrecio()),
                () -> assertEquals(LocalDate.parse("2021-10-07"), result.getFechaLanzamiento()),
                () -> assertEquals(Modelo.DISNEY, result.getModelo()),
                () -> assertEquals(1L, result.getId2()),
                () -> assertNotNull(result.getCod())
        );

        verify(repository, times(1)).save(funko);
    }

    @Test
    void update() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.findById(1L)).thenReturn(Mono.just(funko));
        when(repository.update(funko)).thenReturn(Mono.just(funko));

        var result = service.updateWithNoNotifications(funko).block();

        assertAll("update",
                () -> assertEquals("Rayo McQueen", result.getNombre()),
                () -> assertEquals(100.0, result.getPrecio()),
                () -> assertEquals(LocalDate.parse("2021-10-07"), result.getFechaLanzamiento()),
                () -> assertEquals(Modelo.DISNEY, result.getModelo()),
                () -> assertEquals(1L, result.getId2()),
                () -> assertNotNull(result.getCod())
        );

        verify(repository, times(1)).update(funko);
    }

    @Test
    void updateNoExiste(){
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY)
                .precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.findById(1L)).thenReturn(Mono.empty());

        var result = assertThrows(Exception.class, () -> service.updateWithNoNotifications(funko).block());
        System.out.println(result.getMessage());
        assertTrue(result.getMessage().contains("exceptions.Funko.FunkoNotFoundException: Funko con id 1 no encontrado"));

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deleteById() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();
        when(repository.findById(1L)).thenReturn(Mono.just(funko));
        when(repository.deleteById(1L)).thenReturn(Mono.just(true));

        var result = service.deleteByIdWithoutNotification(1L).block();

        assertEquals(result, funko);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteByIdNoExiste() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();
        when(repository.findById(1L)).thenReturn(Mono.empty());

        var result = assertThrows(Exception.class, () -> service.deleteByIdWithoutNotification(1L).block());
        System.out.println(result.getMessage());
        assertTrue(result.getMessage().contains("exceptions.Funko.FunkoNotFoundException: Funko con id 1 no encontrado"));

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deleteAll() {
        var funko = Funko.builder().cod(UUID.randomUUID()).id2(1L).nombre("Rayo McQueen").modelo(Modelo.DISNEY).precio(100.0).fechaLanzamiento(LocalDate.parse("2021-10-07")).build();

        when(repository.deleteAll()).thenReturn(Mono.empty());

        service.deleteAll().block();

        verify(repository, times(1)).deleteAll();
    }

    @Test
    void expensiveFunkoTest() {
        Funko funko = service.expensiveFunko().block();
        assertAll(
                () -> assertNotNull(funko),
                () -> assertEquals(52.99, funko.getPrecio()),
                () -> assertEquals("Peaky Blinders Tommy", funko.getNombre())
        );
    }

    @Test
    void averagePriceTest() {
        Double averagePrice = service.averagePrice().block();
        assertAll(
                () -> assertNotNull(averagePrice),
                () -> assertEquals(33.51222222222222, averagePrice)
        );
    }

    @Test
    void groupByModeloTest() {
        Map<Modelo, List<Funko>> groupByModelo = service.groupByModelo().block();
        assertAll(
                () -> assertNotNull(groupByModelo),
                () -> assertEquals(4, groupByModelo.size()),
                () -> assertEquals(26, groupByModelo.get(Modelo.MARVEL).size()),
                () -> assertEquals(23, groupByModelo.get(Modelo.ANIME).size()),
                () -> assertEquals(26, groupByModelo.get(Modelo.DISNEY).size()),
                () -> assertEquals(15, groupByModelo.get(Modelo.OTROS).size())
        );
    }

    @Test
    void funkosByModeloTest() {
        Map<Modelo, Long> funkosByModelo = service.funkosByModelo().block();
        assertAll(
                () -> assertNotNull(funkosByModelo),
                () -> assertEquals(4, funkosByModelo.size()),
                () -> assertEquals(26, funkosByModelo.get(Modelo.MARVEL)),
                () -> assertEquals(23, funkosByModelo.get(Modelo.ANIME)),
                () -> assertEquals(26, funkosByModelo.get(Modelo.DISNEY)),
                () -> assertEquals(15, funkosByModelo.get(Modelo.OTROS))
        );
    }

    @Test
    void funkosIn2023Test() {
        List<Funko> funkosIn2023 = service.funkosIn2023().collectList().block();
        assertAll(
                () -> assertNotNull(funkosIn2023),
                () -> assertEquals(57, funkosIn2023.size()),
                () -> assertEquals(2023, funkosIn2023.get(0).getFechaLanzamiento().getYear())
        );
    }

    @Test
    void numberStitchTest() {
        Double numberStitch = service.numberStitch().block();
        assertAll(
                () -> assertNotNull(numberStitch),
                () -> assertEquals(26, numberStitch)
        );
    }

    @Test
    void funkoStitchTest() {
        List<Funko> funkoStitch = service.funkoStitch().collectList().block();
        assertAll(
                () -> assertNotNull(funkoStitch),
                () -> assertEquals(26, funkoStitch.size()),
                () -> assertTrue(funkoStitch.get(0).getNombre().contains("Stitch"))
        );
    }
}