package com.trainguy9512.locomotion.util;

public class Tuple<A, B> {
    private final A a;
    private final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() { return this.a; }
    public B getB() { return this.b; }
}
