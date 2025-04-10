package org.example.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.example.cache.CacheService;
import org.example.cache.Constants;
import org.example.cache.stats.StatsExecutorService;
import org.example.cache.stats.guava.GuavaStats;

import java.util.concurrent.TimeUnit;

public class GuavaCacheService<K,V> implements CacheService<K,V>{
    private final Cache<K,V> cache;
    private final StatsExecutorService statsExecutorService;

    public static class Builder<K,V> {
        private final StatsExecutorService statsExecutorService;

        private int maxSize = Constants.MAX_SIZE;
        private int maxLastTimeAccess = Constants.MAX_LAST_TIME_ACCESS;

        private RemovalListener<K,V> removalListener;

        public Builder(StatsExecutorService statsExecutorService){
            this.statsExecutorService = statsExecutorService;
        }

        public Builder<K,V> removalListener(RemovalListener<K,V> removalListener){
            this.removalListener = removalListener;
            return this;
        }

        public Builder<K, V> maxLastTimeAccess(int maxLastTimeAccess){
            this.maxLastTimeAccess = maxLastTimeAccess;
            return this;
        }

        public Builder<K, V> maxSize(int maxSize){
            this.maxSize = maxSize;
            return this;
        }

        public GuavaCacheService<K, V> build(){
            return new GuavaCacheService<>(this);
        }
    }
    private GuavaCacheService(Builder<K,V> builder){
        int maxSize = builder.maxSize;
        int maxLastTimeAccess = builder.maxLastTimeAccess;
        statsExecutorService = builder.statsExecutorService;
        RemovalListener<K, V> removalListener = builder.removalListener;

        this.cache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterAccess(maxLastTimeAccess, TimeUnit.SECONDS)
            .removalListener(removalListener)
            .recordStats()
            .build();

        statsExecutorService.startStatsTask(new GuavaStats<>(cache));
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
