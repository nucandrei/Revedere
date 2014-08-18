package org.nuc.revedere.util;

public class Container<T> {
    private T content;

    public Container() {
        // empty constructor
    }

    public Container(T initialContent) {
        this.content = initialContent;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public T getContent() {
        return this.content;
    }
}
