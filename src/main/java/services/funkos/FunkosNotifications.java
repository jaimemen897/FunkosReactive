package services.funkos;

import models.Notificacion;
import reactor.core.publisher.Flux;

public interface FunkosNotifications<T> {
    Flux<Notificacion<T>> getNotificationAsFlux();

    void notify(Notificacion<T> notificacion);
}
