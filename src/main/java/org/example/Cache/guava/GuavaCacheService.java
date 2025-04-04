package org.example.Cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.example.Cache.CacheService;
import org.example.Cache.Constants.Constants;
import org.example.Cache.stats.DefaultStatsLogger;
import org.example.Cache.stats.StatsExecutorService;
import org.example.Cache.stats.StatsLogger;
import org.example.Cache.stats.guava.GuavaStats;

import java.util.concurrent.TimeUnit;

public class GuavaCacheService<K,V> implements CacheService<K,V>{
    private final Cache<K,V> cache;
    private final StatsExecutorService statsExecutorService;

    public static <K, V> GuavaCacheService<K, V> create(){
        return new GuavaCacheService<>();
    }

    private GuavaCacheService(){
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(Constants.MAX_SIZE)
                .expireAfterAccess(Constants.MAX_LAST_TIME_ACCESS, TimeUnit.SECONDS)
                .removalListener(new LoggerRemovalListener<>())
                .recordStats()
                .build();

        StatsLogger statsLogger = new DefaultStatsLogger(new GuavaStats<>(cache));
        this.statsExecutorService = StatsExecutorService.create(statsLogger);
        this.statsExecutorService.startStatsTask();
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key,value);
    }

    @Override
    public void close() {
        this.statsExecutorService.shutDown();
    }
}
