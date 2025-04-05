package org.example.cache.stats;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsExecutorService {
    private final ScheduledExecutorService executorService;
    private final StatsLogger statsLogger;
    private final int statsDisplayTime;

    public static StatsExecutorService create(StatsLogger statsLogger, int statsDisplayTime){
        return new StatsExecutorService(statsLogger, statsDisplayTime);
    }

    private StatsExecutorService(StatsLogger statsLogger, int statsDisplayTime){
        this.statsDisplayTime = statsDisplayTime;
        this.statsLogger = statsLogger;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startStatsTask(Stats stats){
        executorService
                .scheduleAtFixedRate(()->statsLogger.stats(stats), statsDisplayTime, statsDisplayTime, TimeUnit.SECONDS);
    }

    public void shutDown(){
        executorService.shutdown();
    }

}
