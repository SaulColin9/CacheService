package org.example.Cache;

public interface CacheService<K, V> {
    V get(K key);

    void put(K key, V value);
}
