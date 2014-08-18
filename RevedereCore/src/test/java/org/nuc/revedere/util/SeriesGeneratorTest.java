package org.nuc.revedere.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SeriesGeneratorTest {
    @Test
    public void testSeriesGenerator() {
        final SeriesGenerator seriesGenerator = new SeriesGenerator("!!!!");
        assertEquals("!!!\"", seriesGenerator.getNext());
    }
    
    @Test
    public void testSeriesGenerator_2() {
        final SeriesGenerator seriesGenerator = new SeriesGenerator("!!!~");
        assertEquals("!!\"!", seriesGenerator.getNext());
    }
    
    @Test
    public void testSeriesGenerator_3() {
        final SeriesGenerator seriesGenerator = new SeriesGenerator("!!~~");
        assertEquals("!\"!!", seriesGenerator.getNext());
    }
}
