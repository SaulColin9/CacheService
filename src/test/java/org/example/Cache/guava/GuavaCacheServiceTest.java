package org.example.Cache.guava;

import org.example.Cache.CacheService;
import org.example.Cache.stats.DefaultStatsLogger;
import org.example.Cache.stats.StatsExecutorService;
import org.example.Cache.stats.StatsLogger;
import static org.example.Cache.Constants.Constants.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GuavaCacheServiceTest {
    private CacheService<Integer, String> cacheService;

    @BeforeEach
    public void setUp(){
        final StatsLogger statsLogger = new DefaultStatsLogger();
        final StatsExecutorService statsExecutorService =
                StatsExecutorService
                        .create(statsLogger, STATS_DISPLAY_TIME);

        cacheService = new GuavaCacheService
                .Builder<Integer,String>(statsExecutorService)
                .maxLastTimeAccess(MAX_LAST_TIME_ACCESS)
                .maxSize(MAX_SIZE)
                .removalListener(new LoggerRemovalListener<>())
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
    public void after_MaxTime_LeastRecentlyUsedEntries_AreEvicted(){
        // arrange
        cacheService.put(1, "A");
        cacheService.put(2, "B");
        cacheService.put(3, "C");

        // act
        cacheService.get(1);
        cacheService.get(2);
        cacheService.get(3);

        // the least recently used entry is 1 at this point

        try {
            Thread.sleep(MAX_LAST_TIME_ACCESS * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // assert
        assertThat(cacheService.get(1)).isNull();
    }

}