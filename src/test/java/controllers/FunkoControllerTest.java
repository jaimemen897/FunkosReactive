package controllers;

import models.Funko;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FunkoControllerTest {

    private FunkoController funkoController;

    @BeforeEach
    void setUp() {
        funkoController = FunkoController.getInstance();
    }

    @Test
    void loadCsvTest() {
        List<Funko> funkoList = funkoController.loadCsv().collectList().block();
        assertFalse(funkoList.isEmpty());
    }
}
