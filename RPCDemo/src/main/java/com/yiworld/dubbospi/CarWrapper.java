package com.yiworld.dubbospi;

// aop
public class CarWrapper implements Car {
    private Car car;

    public CarWrapper(Car car) {
        this.car = car;
    }

    @Override
    public void color() {
        System.out.println("before:");
        car.color();
        System.out.println("after");
    }
}
