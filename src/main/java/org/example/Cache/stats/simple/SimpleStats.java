package org.example.Cache.stats.simple;

import com.google.common.util.concurrent.AtomicDouble;
import org.example.Cache.stats.Stats;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleStats implements Stats {
    private final AtomicInteger evictionCount;
    private final AtomicDouble averageNewPutTime;

    public SimpleStats(AtomicInteger evictionCount, AtomicDouble averageNewPutTime){
        this.evictionCount = evictionCount;
        this.averageNewPutTime = averageNewPutTime;
    }

    @Override
    public long evictionCount() {
        return this.evictionCount.get();
    }

    @Override
    public double averageNewPutTime() {
        return this.averageNewPutTime.get();
    }
}
