package services.funkos;

import models.Funko;
import models.Notificacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import repositories.funkos.FunkoRepositoryImpl;

public class FunkosNotificationsImpl implements FunkosNotifications{
    /*private static FunkoRepositoryImpl INSTANCE = new FunkoRepositoryImpl();

    private final Flux<Notificacion<Funko>> funkosNotificationFlux;
    private FluxSink<Notificacion<Funko>> funkosNotification;

    private FunkoRepositoryImpl() {
        this.funkosNotificationFlux = Flux.<Notificacion<Funko>>create
                (emitter -> this.funkosNotification = emitter).share();
    }

    public static FunkoRepositoryImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FunkoRepositoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Flux<Notificacion<Funko>> getNotificationAsFlux() {
        return funkosNotificationFlux;
    }

    @Override
    public void notify(Notificacion<Funko> notificacion) {
        funkosNotification.next(notificacion);
    }*/
}
