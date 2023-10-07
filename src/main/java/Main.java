import controllers.FunkoController;
import enums.Modelo;
import exceptions.File.ErrorInFile;
import exceptions.File.NotFoundFile;
import exceptions.Funko.FunkoNotStoragedException;
import models.Funko;
import repositories.funkos.FunkoRepositoryImpl;
import routes.Routes;
import services.database.DataBaseManager;
import services.funkos.FunkosServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, NotFoundFile, ErrorInFile, FunkoNotStoragedException {
        FunkoController funkoController = FunkoController.getInstance();
        FunkoRepositoryImpl funkoRepository = FunkoRepositoryImpl.getInstance(DataBaseManager.getInstance());
        FunkosServiceImpl funkosService = FunkosServiceImpl.getInstance(funkoRepository);
        Routes routes = Routes.getInstance();


        System.out.println("-------------------------- OBTENCION DE DATOS --------------------------");
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Callable<List<Funko>> loadCsv = () -> funkoController.loadCsv().get();
        Callable<Funko> expensiveFunko = () -> funkoController.expensiveFunko().get();
        Callable<Double> averagePrice = () -> funkoController.averagePrice().get();
        Callable<Map<Modelo, List<Funko>>> groupByModelo = () -> funkoController.groupByModelo().get();
        Callable<Map<Modelo, Long>> funkosByModelo = () -> funkoController.funkosByModelo().get();
        Callable<List<Funko>> funkosIn2023 = () -> funkoController.funkosIn2023().get();
        Callable<Double> numberStitch = () -> funkoController.numberStitch().get();
        Callable<List<Funko>> funkoStitch = () -> funkoController.funkoStitch().get();


        Future<List<Funko>> future = executorService.submit(loadCsv);
        Future<Funko> future2 = executorService.submit(expensiveFunko);
        Future<Double> future3 = executorService.submit(averagePrice);
        Future<Map<Modelo, List<Funko>>> future4 = executorService.submit(groupByModelo);
        Future<Map<Modelo, Long>> future5 = executorService.submit(funkosByModelo);
        Future<List<Funko>> future6 = executorService.submit(funkosIn2023);
        Future<Double> future7 = executorService.submit(numberStitch);
        Future<List<Funko>> future8 = executorService.submit(funkoStitch);


        try {
            System.out.println("FUNKOS: " + future.get());
            System.out.println("FUNKO MAS CARO: " + future2.get());
            System.out.println("PRECIO MEDIO: " + future3.get());
            System.out.println("AGRUPADOS POR MODELO: " + future4.get());
            System.out.println("NUMERO DE FUNKOS POR MODELO: " + future5.get());
            System.out.println("FUNKOS LANZADOS EN 2023: " + future6.get());
            System.out.println("NUMERO FUNKOS STITCH: " + future7.get());
            System.out.println("FUNKOS DE STITCH: " + future8.get());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }


        System.out.println("-------------------------- INSERTAMOS FUNKOS --------------------------");
        for (Funko funko : funkoController.getFunkos()) {
            funkosService.save(funko);
        }


        System.out.println("-------------------------- MOSTRAMOS FUNKOS DE LA BASE DE DATOS --------------------------");
        funkosService.findAll().forEach(System.out::println);

        System.out.println("-------------------------- EXPORTAMOS FUNKOS A JSON --------------------------");
        CompletableFuture<Void> future9 = funkoRepository.exportJson(routes.getRouteFunkosJson());
        future9.get();


        executorService.shutdown();
        funkosService.close();
    }
}