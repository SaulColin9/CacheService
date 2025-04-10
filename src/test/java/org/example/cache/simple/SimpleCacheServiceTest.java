package org.example.cache.simple;

import org.example.cache.CacheService;
import org.example.cache.stats.DefaultStatsLogger;
import org.example.cache.stats.StatsExecutorService;
import org.example.cache.stats.StatsLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.example.cache.TestConstants.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleCacheServiceTest {
    private CacheService<Integer, String> cacheService;

    @AfterEach
    public void afterEach() throws Exception {
        cacheService.close();
    }

    @BeforeEach
    public void setUp() {
        final StatsLogger statsLogger = new DefaultStatsLogger();
        final StatsExecutorService statsExecutorService =
                StatsExecutorService
                    .create(statsLogger, STATS_DISPLAY_TIME);

        cacheService = new SimpleCacheService
                .Builder<Integer,String>(statsExecutorService)
                .maxLastTimeAccess(MAX_LAST_TIME_ACCESS)
                .maxSize(MAX_SIZE)
                .removalListener(new SimpleLoggerRemovalListener<>())
                .build();
    }

    @Test
    public void givenCacheKey_PutEntryIsReturned() {
        // arrange
        cacheService.put(1, "A");
        // act
        Object cachedValue = cacheService.get(1);
        // assert
        assertThat(cachedValue).isEqualTo("A");
    }

    @Test
    public void givenTwoEntriesWithSameKey_LatestValueIsReturned() {
        // arrange
        cacheService.put(1, "A");
        cacheService.put(1, "B");
        // act
        Object cachedValue = cacheService.get(1);
        // assert
        assertThat(cachedValue).isEqualTo("B");
    }

    @Test
    public void givenNonExistentKey_NullIsReturned(){
        // arrange
        cacheService.put(1, "A");

        // act
        Object cachedValue = cacheService.get(2);

        // assert
        assertThat(cachedValue).isNull();
    }

    @Test
    public void after_MaxTime_LeastFrequentlyUsedEntries_AreEvicted() throws Exception {
        // arrange
        cacheService.put(1, "A");
        cacheService.put(2, "B");
        cacheService.put(3, "C");

        // act
        cacheService.get(1);
        cacheService.get(1);
        cacheService.get(2);

        try {
            Thread.sleep((MAX_LAST_TIME_ACCESS + 1)* 1000);
            // assert
            assertThat(cacheService.get(3)).isNull();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}