package org.nuc.revedere.util;

import java.util.ArrayList;
import java.util.List;

public class SeriesGenerator {
    private final List<CustomCharacter> current;

    public SeriesGenerator(String initialSeed) {
        current = new ArrayList<>(initialSeed.length());
        for (int index = 0; index < initialSeed.length(); index++) {
            current.add(new CustomCharacter(initialSeed.charAt(index)));
        }
    }

    public String getNext() {
        int position = current.size() - 1;
        while (current.get(position).increment()) {
            position--;
        }
        return getCurrent();
    }

    public String getCurrent() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (CustomCharacter customCharacter : current) {
            stringBuilder.append(customCharacter.getCharacter());
        }
        return stringBuilder.toString();
    }
}
