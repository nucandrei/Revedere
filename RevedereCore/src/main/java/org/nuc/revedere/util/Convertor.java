package org.nuc.revedere.util;

public class Convertor<T> {
    @SuppressWarnings("unchecked")
    public T convert(Object object) {
        try {
            return (T) object;
        } catch (ClassCastException e) {
            return null;
        }
    }
}
