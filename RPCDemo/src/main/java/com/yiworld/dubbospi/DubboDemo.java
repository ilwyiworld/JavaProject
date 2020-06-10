package com.yiworld.dubbospi;

import com.alibaba.dubbo.common.extension.ExtensionLoader;

public class DubboDemo {
    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);
        Car redCar = extensionLoader.getExtension("red");
        redCar.color();
    }
}
