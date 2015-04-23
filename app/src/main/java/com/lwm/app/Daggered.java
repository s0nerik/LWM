package com.lwm.app;

public class Daggered {
    public Daggered() {
        Injector.inject(this);
    }
}
