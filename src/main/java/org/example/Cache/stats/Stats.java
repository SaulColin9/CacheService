package org.example.Cache.stats;

public interface Stats {
    long evictionCount();
    double averageNewPutTime();
}
