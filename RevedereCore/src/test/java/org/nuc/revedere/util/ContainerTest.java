package org.nuc.revedere.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContainerTest {
    private static final String CONTENT = "Content";

    @Test
    public void testContainer() {
        Container<String> container = new Container<>();
        assertNull(container.getContent());

        container.setContent(CONTENT);
        assertEquals(CONTENT, container.getContent());
    }
}
