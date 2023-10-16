package services.funkos;

import models.Funko;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import routes.Routes;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunkoStorageTest {
    FunkoStorageImpl funkoStorage;

    @BeforeEach
    void setUp() {
        funkoStorage = FunkoStorageImpl.getInstance();
    }

    @Test
    void loadCsv() {
        List<Funko> funkos = funkoStorage.loadCsv().collectList().block();
        assertFalse(funkos.isEmpty());
    }

    @Test
    void exportJson() {
        funkoStorage.exportJson(Routes.getInstance().getRouteFunkosJson());
        assertTrue(Files.exists(Paths.get(Routes.getInstance().getRouteFunkosJson())));
    }
}