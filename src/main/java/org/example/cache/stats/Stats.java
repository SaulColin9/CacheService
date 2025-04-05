package org.example.cache.stats;

public interface Stats {
    long evictionCount();
    double averageNewPutTime();
}
