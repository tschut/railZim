package com.spacemangames.util;

import org.apache.commons.collections4.Predicate;

public class ThreadUtils {
    public static <T> void sleepUntil(Predicate<T> predicate, long sleepTime, T predicateArg) {
        while (!predicate.evaluate(predicateArg)) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void silentSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
