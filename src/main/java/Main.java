import controllers.FunkoController;
import exceptions.File.ErrorInFile;
import exceptions.File.NotFoundFile;
import exceptions.Funko.FunkoNotFoundException;
import models.Funko;
import repositories.funkos.FunkoRepositoryImpl;
import routes.Routes;
import services.database.DataBaseManager;
import services.funkos.FunkosNotificationsImpl;
import services.funkos.FunkosServiceImpl;

public class Main {
    public static void main(String[] args) throws NotFoundFile, ErrorInFile, FunkoNotFoundException {
        FunkoController funkoController = FunkoController.getInstance();
        DataBaseManager dataBaseManager = DataBaseManager.getInstance();
        FunkoRepositoryImpl funkoRepository = FunkoRepositoryImpl.getInstance(dataBaseManager);
        FunkosServiceImpl funkosService = FunkosServiceImpl.getInstance(funkoRepository, FunkosNotificationsImpl.getInstance());
        Routes routes = Routes.getInstance();
        FunkosNotificationsImpl notifications = FunkosNotificationsImpl.getInstance();

        notifications.getNotificationAsFlux().subscribe(
                notification -> {
                    switch (notification.getTipo()) {
                        case NEW -> System.out.println("🟢 Funko insertado: " + notification.getContenido());
                        case UPDATED ->
                                System.out.println("🟠 Funko actualizado: " + notification.getContenido());
                        case DELETED ->
                                System.out.println("🔴 Funko eliminado: " + notification.getContenido());
                        case INFO -> System.out.println("🔵 " + notification.getContenido());
                    }
                },
                error -> System.out.println("Error: " + error.getMessage()),
                () -> System.out.println("Obtención de funkos completada")
        );
        funkoController.loadCsv();

        funkoController.expensiveFunko().subscribe();

        funkoController.averagePrice().subscribe();

        funkoController.groupByModelo().subscribe();

        funkoController.funkosByModelo().subscribe();

        funkoController.funkosIn2023().subscribe();

        funkoController.funkoStitch().subscribe();

        funkoController.numberStitch().subscribe();



        for (Funko funko : funkoController.getFunkos()) {
            funkosService.save(funko).subscribe();
        }


        funkosService.findById(80L).subscribe(
                funkos -> System.out.println("Funko: " + funkos),
                error -> System.err.println("Error al obtener todos los funkos: " + error.getMessage()),
                () -> System.out.println("Obtención de funkos completada")
        );

        funkosService.findByNombre("Stitch").subscribe(
                funkos -> System.out.println("Funko: " + funkos),
                error -> System.err.println("Error al obtener todos los funkos: " + error.getMessage()),
                () -> System.out.println("Obtención de funkos completada")
        );

        funkoRepository.exportJson(routes.getRouteFunkosJson()).subscribe();


        System.exit(0);
    }
}