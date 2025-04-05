package org.example.cache.guava;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerRemovalListener<K,V> implements RemovalListener<K,V> {
    private static final Logger logger = LoggerFactory.getLogger(LoggerRemovalListener.class);

    @Override
    public void onRemoval(RemovalNotification<K, V> notification) {
        logger.info("Removed entry: { key: {}, value: {}, cause:{} }", notification.getKey(), notification.getValue(), notification.getCause());
    }
}
