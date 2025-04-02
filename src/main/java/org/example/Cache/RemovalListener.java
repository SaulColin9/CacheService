package org.example.Cache;

import org.example.Cache.Constants.RemovalCause;

@FunctionalInterface
public interface RemovalListener<K, V> {
    void onRemoval(K key, V value, RemovalCause cause);
}