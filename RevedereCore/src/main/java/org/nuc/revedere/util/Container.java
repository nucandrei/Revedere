package org.nuc.revedere.util;

public class Container<T> {
    private T content;

    public void setContent(T content) {
        this.content = content;
    }

    public T getContent() {
        return this.content;
    }
}
