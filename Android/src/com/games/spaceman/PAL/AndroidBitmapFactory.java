package com.games.spaceman.pal;

import java.util.HashMap;
import java.util.Map;

import com.spacemangames.pal.IBitmap;
import com.spacemangames.pal.IBitmapFactory;

public class AndroidBitmapFactory implements IBitmapFactory {
    private final Map<String, IBitmap> cache = new HashMap<String, IBitmap>();

    public IBitmap createBitmap(String aResource, boolean lazyLoading) {
        IBitmap bitmap;
        if (!cache.containsKey(aResource)) {
            bitmap = new AndroidBitmap(aResource, lazyLoading);
            cache.put(aResource, bitmap);
        } else {
            bitmap = cache.get(aResource);
        }

        return bitmap;
    }
}
