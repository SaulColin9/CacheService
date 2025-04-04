package org.example.Cache.stats.guava;

import com.google.common.cache.Cache;
import org.example.Cache.stats.Stats;

public class GuavaStats<K,V> implements Stats {
    private final Cache<K,V> cache;

    public GuavaStats(Cache<K,V> cache){
        this.cache = cache;
    }

    @Override
    public long evictionCount() {
        return cache.stats().evictionCount();
    }

    @Override
    public double averageNewPutTime() {
        return cache.stats().averageLoadPenalty();
    }
}
