package controllers;

import enums.Modelo;
import models.Funko;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import services.funkos.FunkosNotificationsImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class FunkoControllerTest {

    private FunkoController funkoController;

    @BeforeEach
    void setUp() {
        funkoController = FunkoController.getInstance(FunkosNotificationsImpl.getInstance());
        funkoController.loadCsvWithOutNotify();
    }

    @AfterEach
    void tearDown() {
        funkoController.getFunkos().clear();
    }

    @Test
    void loadCsvTest() {
        assertAll(
                () -> assertNotNull(funkoController.getFunkos()),
                () -> assertEquals(90, funkoController.getFunkos().size())
        );
    }

    @Test
    void expensiveFunkoTest() {
        Funko funko = funkoController.expensiveFunko().block();
        assertAll(
                () -> assertNotNull(funko),
                () -> assertEquals(52.99, funko.getPrecio()),
                () -> assertEquals("Peaky Blinders Tommy", funko.getNombre())
        );
    }

    @Test
    void averagePriceTest() {
        Double averagePrice = funkoController.averagePrice().block();
        assertAll(
                () -> assertNotNull(averagePrice),
                () -> assertEquals(33.51222222222222, averagePrice)
        );
    }

    @Test
    void groupByModeloTest() {
        Map<Modelo, List<Funko>> groupByModelo = funkoController.groupByModelo().block();
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
        Map<Modelo, Long> funkosByModelo = funkoController.funkosByModelo().block();
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
        List<Funko> funkosIn2023 = funkoController.funkosIn2023().collectList().block();
        assertAll(
                () -> assertNotNull(funkosIn2023),
                () -> assertEquals(57, funkosIn2023.size()),
                () -> assertEquals(2023, funkosIn2023.get(0).getFechaLanzamiento().getYear())
        );
    }

    @Test
    void numberStitchTest() {
        Double numberStitch = funkoController.numberStitch().block();
        assertAll(
                () -> assertNotNull(numberStitch),
                () -> assertEquals(26, numberStitch)
        );
    }

    @Test
    void funkoStitchTest() {
        List<Funko> funkoStitch = funkoController.funkoStitch().collectList().block();
        assertAll(
                () -> assertNotNull(funkoStitch),
                () -> assertEquals(26, funkoStitch.size()),
                () -> assertTrue(funkoStitch.get(0).getNombre().contains("Stitch"))
        );
    }
}
