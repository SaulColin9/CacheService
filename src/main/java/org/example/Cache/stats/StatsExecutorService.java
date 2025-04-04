package org.example.Cache.stats;


import org.example.Cache.Constants.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsExecutorService {
    private final ScheduledExecutorService executorService;
    private final StatsLogger statsLogger;

    public static StatsExecutorService create(StatsLogger statsLogger){
        return new StatsExecutorService(statsLogger);
    }

    private StatsExecutorService(StatsLogger statsLogger){
        this.statsLogger = statsLogger;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startStatsTask(){
        executorService
                .scheduleAtFixedRate(statsLogger::stats, Constants.STATS_DISPLAY_TIME, Constants.STATS_DISPLAY_TIME, TimeUnit.SECONDS);
    }

    public void shutDown(){
        executorService.shutdown();
    }

}
