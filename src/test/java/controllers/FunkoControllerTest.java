package controllers;

import enums.Modelo;
import models.Funko;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class FunkoControllerTest {

    private FunkoController funkoController;

    @BeforeEach
    void setUp() throws Exception {
        funkoController = FunkoController.getInstance();
        funkoController.getFunkos().clear();
    }

    @AfterEach
    void tearDown() {
        funkoController.getFunkos().clear();
    }

    @Test
    void loadCsv() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        assertAll(
                () -> assertNotNull(funkoController.getFunkos()),
                () -> assertEquals(90, funkoController.getFunkos().size())
        );
    }

    @Test
    void expensiveFunko() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<Funko> expensiveFunko = () -> funkoController.expensiveFunko().block();
        expensiveFunko.call();
        assertAll(
                () -> assertNotNull(funkoController.expensiveFunko()),
                () -> assertEquals(52.99, funkoController.expensiveFunko().block().getPrecio()),
                () -> assertEquals("Peaky Blinders Tommy", funkoController.expensiveFunko().block().getNombre())

        );
    }

    @Test
    void averagePrice() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<Double> averagePrice = () -> funkoController.averagePrice().block();
        averagePrice.call();
        assertAll(
                () -> assertNotNull(funkoController.averagePrice()),
                () -> assertEquals(33.51222222222222, funkoController.averagePrice().block())
        );
    }

    @Test
    void groupByModelo() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<Map<Modelo, List<Funko>>> groupByModelo = () -> funkoController.groupByModelo().block();
        groupByModelo.call();
        assertAll(
                () -> assertNotNull(funkoController.groupByModelo()),
                () -> assertEquals(4, funkoController.groupByModelo().block().size())
        );
    }

    @Test
    void funkosByModelo() throws Exception {
        funkoController.loadCsv();
        Map<Modelo, Long> funkosByModelo = () -> funkoController.funkosByModelo().block();
        funkosByModelo.call();
        assertAll(
                () -> assertNotNull(funkoController.funkosByModelo()),
                () -> assertEquals(4, funkoController.funkosByModelo().block().size()),
                () -> assertEquals(26, funkoController.funkosByModelo().block().get(Modelo.MARVEL)),
                () -> assertEquals(23, funkoController.funkosByModelo().block().get(Modelo.ANIME)),
                () -> assertEquals(26, funkoController.funkosByModelo().block().get(Modelo.DISNEY)),
                () -> assertEquals(15, funkoController.funkosByModelo().block().get(Modelo.OTROS))
        );
    }

    @Test
    void funkosIn2023() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<List<Funko>> funkosIn2023 = () -> funkoController.funkosIn2023().block();
        funkosIn2023.call();
        assertAll(
                () -> assertNotNull(funkoController.funkosIn2023()),
                () -> assertEquals(57, funkoController.funkosIn2023().block().size()),
                () -> assertEquals(2023, funkoController.funkosIn2023().block().get(0).getFechaLanzamiento().getYear())
        );
    }

    @Test
    void numberStitch() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<Double> numberStitch = () -> funkoController.numberStitch().block();
        numberStitch.call();
        assertAll(
                () -> assertNotNull(funkoController.numberStitch()),
                () -> assertEquals(26, funkoController.numberStitch().block())
        );
    }

    @Test
    void funkoStitch() throws Exception {
        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().block();
        loadCsv.call();
        Callable<List<Funko>> funkoStitch = () -> funkoController.funkoStitch().block();
        funkoStitch.call();
        var result = funkoController.funkoStitch().block().get(0).getNombre().contains("Stitch");
        assertAll(
                () -> assertNotNull(funkoController.funkoStitch()),
                () -> assertEquals(26, funkoController.funkoStitch().block().size()),
                () -> assertTrue(result)
        );
    }
}
}