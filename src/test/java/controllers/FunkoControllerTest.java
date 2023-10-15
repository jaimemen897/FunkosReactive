package controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunkoControllerTest {

    private FunkoController funkoController;

    @BeforeEach
    void setUp() {
        funkoController = FunkoController.getInstance();
    }

    @Test
    void loadCsvTest() {
        funkoController.loadCsv().subscribe();
        assertAll(
                () -> assertNotNull(funkoController.getFunkosList()),
                () -> assertEquals(90, funkoController.getFunkosList().size())
        );
    }
}
