package com.memeticame.memeticame.cache;

import android.util.LruCache;

/**
 * Created by ESTEBANFML on 17-10-2017.
 */

public class LRUCache {

    private static LRUCache instance;
    private LruCache<Object, Object> lru;

    private LRUCache() {

        lru = new LruCache<Object, Object>(2048);

    }

    public static LRUCache getInstance() {

        if (instance == null) {

            instance = new LRUCache();
        }

        return instance;

    }

    public LruCache<Object, Object> getLru() {
        return lru;
    }

}

