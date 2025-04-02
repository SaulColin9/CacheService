package org.example;

import org.example.Cache.LoggerRemovalListener;
import org.example.Cache.SimpleCacheService;

public class App
{

    public static void main( String[] args )
    {
        try(SimpleCacheService<Integer, String> cache = SimpleCacheService.create()){
        }
    }
}
