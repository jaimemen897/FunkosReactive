package services.funkos;

import models.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FunkoCacheImpl implements FunkoCache {
    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
    private final int maxSize = 10;
    private final Map<Long, Funko> cache;
    Lock lock = new ReentrantLock();
    private final ScheduledExecutorService cleaner;


    public FunkoCacheImpl() {
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Funko> eldest) {
                return size() > maxSize;
            }
        };
        //Crea el programador para la limpieza automatica
        this.cleaner = Executors.newSingleThreadScheduledExecutor();

        //Programar la limpieza cada dos minutos
        this.cleaner.scheduleAtFixedRate(this::clear, 2, 2, TimeUnit.MINUTES);
    }

    @Override
    public void put(Long key, Funko value) {
        lock.lock();
        try {
            logger.debug("Añadiendo funko a la cache con id:" + key + " y valor:" + value);
            cache.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Funko get(Long key) {
        lock.lock();
        try {
            logger.debug("Obteniendo funko de la cache con id:" + key);
            if (cache.get(key) == null) {
                logger.error("No se ha encontrado el funko con id:" + key);
            }
            return cache.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(Long key) {
        lock.lock();
        try {
            logger.debug("Eliminando funko de la cache con id:" + key);
            cache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            cache.entrySet().removeIf(entry -> {
                boolean shouldRemove = entry.getValue().getUpdatedAt().plusMinutes(2).isBefore(LocalDateTime.now());
                if (shouldRemove) {
                    logger.debug("Eliminando funko de la cache con id:" + entry.getKey());
                }
                return shouldRemove;
            });
        } finally {
            lock.unlock();
        }

    }

    @Override
    public synchronized void shutdown() {
        logger.debug("Cerrando cache");
        cleaner.shutdown();
    }
}
