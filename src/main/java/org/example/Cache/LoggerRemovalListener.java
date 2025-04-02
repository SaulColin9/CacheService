package org.example.Cache;

import org.example.Cache.Constants.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerRemovalListener<K, V> implements RemovalListener<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(LoggerRemovalListener.class);

    @Override
    public void onRemoval(K key, V value, RemovalCause cause) {
        logger.info("Removed entry: { key: {}, value: {}, cause:{} }", key, value, cause);
    }
}
