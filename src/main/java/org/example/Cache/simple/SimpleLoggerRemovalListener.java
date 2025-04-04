package org.example.Cache.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLoggerRemovalListener<K, V> implements RemovalListener<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerRemovalListener.class);

    @Override
    public void onRemoval(K key, V value, RemovalCause cause) {
        logger.info("Removed entry: { key: {}, value: {}, cause:{} }", key, value, cause);
    }
}
