package com.yiworld;

import org.openjdk.jol.info.ClassLayout;

public class ObjectTest {
    static L l=new L();
    public static void main(String[] args) {
        System.out.println(ClassLayout.parseInstance(l).toPrintable());
    }

    private static class L {
    }

}
