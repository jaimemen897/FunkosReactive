import controllers.FunkoController;
import exceptions.File.ErrorInFile;
import exceptions.File.NotFoundFile;
import exceptions.Funko.FunkoNotFoundException;
import exceptions.Funko.FunkoNotStoragedException;
import models.Funko;
import repositories.funkos.FunkoRepositoryImpl;
import services.database.DataBaseManager;
import services.funkos.FunkosNotificationsImpl;
import services.funkos.FunkosServiceImpl;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, NotFoundFile, ErrorInFile, FunkoNotStoragedException, FunkoNotFoundException {
        FunkoController funkoController = FunkoController.getInstance();
        DataBaseManager dataBaseManager = DataBaseManager.getInstance();
        FunkoRepositoryImpl funkoRepository = FunkoRepositoryImpl.getInstance(dataBaseManager);
        FunkosServiceImpl funkosService = FunkosServiceImpl.getInstance(funkoRepository,FunkosNotificationsImpl.getInstance());

        funkoController.loadCsv().subscribe();

        funkoController.expensiveFunko().subscribe(
                funko -> System.out.println("Funko más caro: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.averagePrice().subscribe(
                average -> System.out.println("Precio medio: " + average),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.groupByModelo().subscribe(
                funko -> System.out.println("Funkos agrupados por modelo: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.funkosByModelo().subscribe(
                funko -> System.out.println("Numero de funkos por modelo: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.funkosIn2023().subscribe(
                funko -> System.out.println("Funkos que salieron en 2023: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.funkoStitch().subscribe(
                funko -> System.out.println("Funkos de Stitch: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        funkoController.numberStitch().subscribe(
                funko -> System.out.println("Numero de funkos de Stitch: " + funko),
                error -> System.out.println("Error: " + error.getMessage())
        );

        for (Funko funko: funkoController.getFunkos()) {
            funkoRepository.save(funko).subscribe();
        }


        /*funkosService.findAll().collectList().subscribe(
                funkos -> System.out.println("Funkos: " + funkos),
                error -> System.err.println("Error al obtener todos los funkos: " + error.getMessage()),
                () -> System.out.println("Obtención de funkos completada")
        );*/

        funkosService.getNotifications().subscribe(
                notificacion -> {
                    switch (notificacion.getTipo()) {
                        case NEW:
                            System.out.println("🟢 Funko insertado: " + notificacion.getContenido());
                            break;
                        case UPDATED:
                            System.out.println("🟠 Funko actualizado: " + notificacion.getContenido());
                            break;
                        case DELETED:
                            System.out.println("🔴 Funko eliminado: " + notificacion.getContenido());
                            break;
                    }
                },
                error -> System.err.println("Se ha producido un error: " + error),
                () -> System.out.println("Completado")
        );

        funkosService.findAll().collectList().subscribe(
                funkos -> System.out.println("Funkos: " + funkos),
                error -> System.err.println("Error al obtener todos los funkos: " + error.getMessage()),
                () -> System.out.println("Obtención de funkos completada")
        );

        System.exit(0);
    }
}