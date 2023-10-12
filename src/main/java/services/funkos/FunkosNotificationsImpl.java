package services.funkos;

import models.Funko;
import models.Notificacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class FunkosNotificationsImpl implements FunkosNotifications<Funko> {

    private static FunkosNotificationsImpl INSTANCE = new FunkosNotificationsImpl();

    private final Flux<Notificacion<Funko>> funkosNotificationFlux;
    private FluxSink<Notificacion<Funko>> funkosNotification;

    private FunkosNotificationsImpl() {
        this.funkosNotificationFlux = Flux.<Notificacion<Funko>>create(emitter -> this.funkosNotification = emitter).share();
    }

    public static FunkosNotificationsImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FunkosNotificationsImpl();
        }
        return INSTANCE;
    }

    @Override
    public Flux<Notificacion<Funko>> getNotificationAsFlux() {
        return funkosNotificationFlux;
    }

    @Override
    public void notify(Notificacion notificacion) {
        funkosNotification.next(notificacion);
    }
}