package org.nuc.revedere.util;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BidirectionalMapTest {
    private BidirectionMap<Integer, String> testedMap;
    private final Integer KEY = 1234;
    private final String VALUE = "dummy value";
    private final int NO_KEYS = 10;

    @Before
    public void setUp() {
        testedMap = new BidirectionMap<>();
    }

    @Test
    public void testEmptyBidirectionalMap() {
        assertEquals(0, testedMap.size());
    }

    @Test
    public void testPutKeyAndValue() {
        testedMap.put(KEY, VALUE);
        assertEquals(1, testedMap.size());
        assertEquals(VALUE, testedMap.getValue(KEY));
        assertEquals(KEY, testedMap.getKey(VALUE));
        assertTrue(testedMap.containsKey(KEY));
        assertTrue(testedMap.containsValue(VALUE));
    }

    @Test
    public void testRemoveKey() {
        testedMap.put(KEY, VALUE);
        assertEquals(VALUE, testedMap.removeKey(KEY));
        assertNull(testedMap.removeKey(KEY));
        assertNull(testedMap.removeValue(VALUE));
        assertFalse(testedMap.containsKey(KEY));
        assertFalse(testedMap.containsValue(VALUE));
        assertEquals(0, testedMap.size());
    }

    @Test
    public void testRemoveNullKeyOrValue() {
        assertNull(testedMap.removeKey(KEY));
        assertNull(testedMap.removeValue(VALUE));
    }

    @Test
    public void testRemoveValue() {
        testedMap.put(KEY, VALUE);
        assertEquals(KEY, testedMap.removeValue(VALUE));
        assertNull(testedMap.removeValue(VALUE));
        assertNull(testedMap.removeKey(KEY));
        assertFalse(testedMap.containsKey(KEY));
        assertFalse(testedMap.containsValue(VALUE));
        assertEquals(0, testedMap.size());
    }

    @Test
    public void testGetKeysAndValues() {
        final List<Integer> keys = new LinkedList<Integer>();
        final List<String> values = new LinkedList<String>();
        for (int i = 0; i < NO_KEYS; i++) {
            Integer key = new Integer(i);
            String value = i + "";
            testedMap.put(key, value);
            keys.add(key);
            values.add(value);
        }
        final Collection<String> extractedValues = testedMap.values();
        final Collection<Integer> extractedKeys = testedMap.keys();
        
        assertEquals(NO_KEYS, extractedValues.size());
        assertEquals(NO_KEYS, extractedKeys.size());
        assertTrue(keys.containsAll(extractedKeys));
        assertTrue(extractedKeys.containsAll(keys));
        
        assertTrue(values.containsAll(extractedValues));
        assertTrue(extractedValues.containsAll(values));
        
    }
}
