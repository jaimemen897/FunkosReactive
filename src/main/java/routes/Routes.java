package routes;

import lombok.Getter;

import java.io.File;

@Getter
public class Routes {
    private static Routes instance;
    private final String routeFunkosCsv = "src" + File.separator + "data" + File.separator + "funkos.csv";
    private final String routeFunkosJson = "src" + File.separator + "data" + File.separator + "funkos.json";
    private final String routeDirResources = "src" + File.separator + "main" + File.separator + "resources" + File.separator;

    public static Routes getInstance() {
        if (instance == null) {
            instance = new Routes();
        }
        return instance;
    }
}
