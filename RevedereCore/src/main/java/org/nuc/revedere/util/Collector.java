package org.nuc.revedere.util;

import java.util.ArrayList;
import java.util.List;

public abstract class Collector<T> {
    private final List<CollectorListener<T>> listenerList = new ArrayList<>();

    public void agregate(T update) {
        for (CollectorListener<T> listener : listenerList) {
            listener.onUpdate(this, update);
        }
    }

    public void addListener(CollectorListener<T> listener) {
        listenerList.add(listener);
        listener.onUpdate(this, getCurrentState());
    }

    public void removeListener(CollectorListener<T> listener) {
        listenerList.remove(listener);
    }

    public interface CollectorListener<E> {
        public void onUpdate(Collector<E> source, E update);
    }
    
    public abstract T getCurrentState();
}
