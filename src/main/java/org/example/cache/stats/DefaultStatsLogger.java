package org.example.cache.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultStatsLogger implements StatsLogger{
    private static final Logger logger = LoggerFactory.getLogger(DefaultStatsLogger.class);

    @Override
    public void stats(Stats stats) {
        logger.info("Average put time: {} ns", stats.averageNewPutTime());
        logger.info("Total Number of cache evictions: {}", stats.evictionCount());
    }
}
