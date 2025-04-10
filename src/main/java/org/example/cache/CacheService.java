package org.example.cache;

public interface CacheService<K, V> extends AutoCloseable{
    V get(K key);

    void put(K key, V value);
}
