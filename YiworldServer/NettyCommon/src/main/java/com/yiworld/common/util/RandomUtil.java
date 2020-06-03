package com.yiworld.common.util;

public class RandomUtil {
    public static int getRandom() {
        double random = Math.random();
        return (int) (random * 100);
    }
}
