package org.nuc.revedere.util;

public class Tuple<T, U> {
    private T tItem;
    private U uItem;

    public Tuple(T tItem, U uItem) {
        this.tItem = tItem;
        this.uItem = uItem;
    }

    public T getTItem() {
        return this.tItem;
    }

    public U getUItem() {
        return this.uItem;
    }
}
