package org.example.Cache.simple;

import com.google.common.util.concurrent.AtomicDouble;
import org.example.Cache.CacheService;
import org.example.Cache.Constants.*;
import org.example.Cache.stats.DefaultStatsLogger;
import org.example.Cache.stats.simple.SimpleStats;
import org.example.Cache.stats.StatsExecutorService;
import org.example.Cache.stats.StatsLogger;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Eviction logic - LFU
 * when the cache is full (that is either we reached 100k entries or KB)
 * search for the entries with the lowest frequency and remove them from cache
 * the eviction will also be called out every 5 seconds
 */
public class SimpleCacheService<K, V> implements CacheService<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Map<K, Integer> frequency = new ConcurrentHashMap<>();
    private final Map<Integer, ConcurrentLinkedDeque<K>> minFrequency = new ConcurrentHashMap<>();
    private int minFrequencyAvailable = 0;

    private final AtomicInteger evictionCount = new AtomicInteger(0);
    private final AtomicLong totalPutTime = new AtomicLong(0);
    private final AtomicLong putOperationCount = new AtomicLong(0);
    private final AtomicDouble atomicDouble = new AtomicDouble(0);

    private final ScheduledExecutorService evictionExecutorService;
    private final StatsExecutorService statsExecutorService;
    private final Object lock = new Object();

    private final RemovalListener<K, V> removalListener = new SimpleLoggerRemovalListener<>();

    public static <K, V> SimpleCacheService<K, V> create() {
        return new SimpleCacheService<>(Executors.newSingleThreadScheduledExecutor());
    }

    private SimpleCacheService(ScheduledExecutorService evictionExecutorService) {
        this.evictionExecutorService = evictionExecutorService;

        final StatsLogger statsLogger = new DefaultStatsLogger(new SimpleStats(evictionCount, atomicDouble));
        this.statsExecutorService = StatsExecutorService.create(statsLogger);

        this.statsExecutorService.startStatsTask();
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

            minFrequencyAvailable = 1;
            trackPutTime(System.nanoTime() - startTime);
        }
    }

    private boolean isCacheAvailable() {
        synchronized (lock) {
            return cache.size() < Constants.MAX_SIZE;
        }
    }

    private void cacheEviction() {
        synchronized (lock) {
            K evictedKey = minFrequency.get(minFrequencyAvailable).iterator().next();
            minFrequency.get(minFrequencyAvailable).remove(evictedKey);

            if (minFrequency.get(minFrequencyAvailable).isEmpty()) minFrequency.remove(minFrequencyAvailable);

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
                if (minFrequencyAvailable == entryFrequency) minFrequencyAvailable++;
            }

            minFrequency.computeIfAbsent(entryFrequency + 1, k -> new ConcurrentLinkedDeque<>()).add(key);
        }
    }

    private void trackPutTime(long elapsedTime) {
        synchronized (lock){
            totalPutTime.addAndGet(elapsedTime);
            putOperationCount.incrementAndGet();
            atomicDouble.set((double) totalPutTime.get() / putOperationCount.get());
        }
    }

    private void startCleanupTask() {
        evictionExecutorService
                .scheduleAtFixedRate(this::cacheEviction, Constants.MAX_LAST_TIME_ACCESS, Constants.MAX_LAST_TIME_ACCESS, TimeUnit.SECONDS);
    }


    @Override
    public void close() {
        evictionExecutorService.shutdown();
        statsExecutorService.shutDown();
    }

    @Override
    public String toString() {
        return "SimpleCacheService{" + "frequency=" + frequency + ", cache=" + cache + '}';
    }
}

