package org.example.Cache.simple;

import com.google.common.util.concurrent.AtomicDouble;
import org.example.Cache.CacheService;
import org.example.Cache.Constants.*;
import org.example.Cache.stats.simple.SimpleStats;
import org.example.Cache.stats.StatsExecutorService;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleCacheService<K, V> implements CacheService<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Map<K, Integer> frequency = new ConcurrentHashMap<>();
    private final Map<Integer, ConcurrentLinkedDeque<K>> minFrequency = new ConcurrentHashMap<>();

    private final int maxSize;
    private final int maxLastTimeAccess;

    private final AtomicInteger evictionCount = new AtomicInteger(0);
    private final AtomicLong totalPutTime = new AtomicLong(0);
    private final AtomicLong putOperationCount = new AtomicLong(0);
    private final AtomicDouble averageNewPutTime = new AtomicDouble(0);
    private final AtomicInteger minFrequencyAvailable = new AtomicInteger(0);

    private final ScheduledExecutorService evictionExecutorService;
    private final StatsExecutorService statsExecutorService;
    private final Object lock = new Object();

    private final RemovalListener<K, V> removalListener;

    public static class Builder<K,V> {
        private final StatsExecutorService statsExecutorService;

        private RemovalListener<K, V> removalListener = (key, value, cause) -> {};
        private ScheduledExecutorService evictionExecutorService = Executors.newSingleThreadScheduledExecutor();
        private int maxSize = Constants.MAX_SIZE;
        private int maxLastTimeAccess = Constants.MAX_LAST_TIME_ACCESS;

        public Builder(StatsExecutorService statsExecutorService){
            this.statsExecutorService = statsExecutorService;
        }

        public Builder<K,V> removalListener(RemovalListener<K,V> removalListener){
            this.removalListener = removalListener;
            return this;
        }

        public Builder<K, V> evictionExecutorService(ScheduledExecutorService evictionExecutorService){
            this.evictionExecutorService = evictionExecutorService;
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

        public SimpleCacheService<K, V> build(){
            return new SimpleCacheService<>(this);
        }
    }

    private SimpleCacheService(Builder<K,V> builder){
        maxSize = builder.maxSize;
        maxLastTimeAccess = builder.maxLastTimeAccess;
        statsExecutorService = builder.statsExecutorService;
        removalListener = builder.removalListener;
        evictionExecutorService = builder.evictionExecutorService;

        statsExecutorService.startStatsTask(new SimpleStats(evictionCount, averageNewPutTime));
        startCleanupTask();
    }

    @Override
    public V get(K key) {
        V value = cache.get(key);
        if (value == null) return null;
        updateFrequency(key);

        return value;
    }

    @Override
    public void put(K key, V value) {
        long startTime = System.nanoTime();

        synchronized (lock) {
            if (cache.containsKey(key)) {
                V oldValue = cache.put(key, value);
                if (oldValue != null) removalListener.onRemoval(key, oldValue, RemovalCause.UPDATED);

                updateFrequency(key);
                return;
            }

            if (!isCacheAvailable()) cacheEviction();

            cache.put(key, value);
            frequency.put(key, 1);
            minFrequency.computeIfAbsent(1, k -> new ConcurrentLinkedDeque<>()).add(key);

            minFrequencyAvailable.set(1);
            trackPutTime(System.nanoTime() - startTime);
        }
    }

    private boolean isCacheAvailable() {
        synchronized (lock) {
            return cache.size() < maxSize;
        }
    }

    private void cacheEviction() {
        synchronized (lock) {
            K evictedKey = minFrequency.get(minFrequencyAvailable.get()).iterator().next();
            minFrequency.get(minFrequencyAvailable.get()).remove(evictedKey);

            if (minFrequency.get(minFrequencyAvailable.get()).isEmpty()) minFrequency.remove(minFrequencyAvailable.get());

            V evictedValue = cache.remove(evictedKey);
            frequency.remove(evictedKey);
            evictionCount.incrementAndGet();

            if (evictedKey != null) removalListener.onRemoval(evictedKey, evictedValue, RemovalCause.EVICTED);
        }
    }

    private void updateFrequency(K key) {
        synchronized (lock) {
            int entryFrequency = frequency.get(key);

            frequency.put(key, entryFrequency + 1);

            minFrequency.get(entryFrequency).remove(key);

            if (minFrequency.get(entryFrequency).isEmpty()) {
                minFrequency.remove(entryFrequency);
                if (minFrequencyAvailable.get() == entryFrequency) minFrequencyAvailable.incrementAndGet();
            }

            minFrequency.computeIfAbsent(entryFrequency + 1, k -> new ConcurrentLinkedDeque<>()).add(key);
        }
    }

    private void trackPutTime(long elapsedTime) {
        synchronized (lock){
            totalPutTime.addAndGet(elapsedTime);
            putOperationCount.incrementAndGet();
            averageNewPutTime.set((double) totalPutTime.get() / putOperationCount.get());
        }
    }

    private void startCleanupTask() {
        evictionExecutorService
                .scheduleAtFixedRate(this::cacheEviction, maxLastTimeAccess, maxLastTimeAccess, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        evictionExecutorService.shutdown();
        statsExecutorService.shutDown();
    }
}

