package org.example.Cache.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultStatsLogger implements StatsLogger{
    private static final Logger logger = LoggerFactory.getLogger(DefaultStatsLogger.class);
    private final Stats stats;

    public DefaultStatsLogger(Stats stats){
        this.stats = stats;
    }
    @Override
    public void stats() {
        logger.info("Average put time: {} ns", stats.averageNewPutTime());
        logger.info("Total Number of cache evictions: {}", stats.evictionCount());
    }
}
