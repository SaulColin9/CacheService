package org.example.Cache.simple;

@FunctionalInterface
public interface RemovalListener<K, V> {
    void onRemoval(K key, V value, RemovalCause cause);
}