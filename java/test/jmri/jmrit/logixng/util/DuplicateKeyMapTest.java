package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DuplicateKeyMap
 * 
 * @author Daniel Bergqvist 2019
 */
public class DuplicateKeyMapTest {

    private DuplicateKeyMap<String,String> t;

    private void expectException(Runnable r, Class<? extends Exception> exceptionClass, String errorMessage) {
        Exception e = assertThrowsExactly( exceptionClass, () -> r.run(), "Exception is thrown");
        assertEquals( errorMessage, e.getMessage(), "Exception message is correct");
    }

    @Test
    public void testCtor() {
        assertNotNull( t, "not null");
    }

    @Test
    public void testSize() {
        assertEquals( 6, t.size(), "size is correct");
        assertFalse( t.isEmpty(), "map is not empty");

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        DuplicateKeyMap<String,String> t2 = new DuplicateKeyMap<>();
        assertEquals( 0, t2.size(), "size is correct");
        assertTrue( t2.isEmpty(), "map is empty");
    }

    @Test
    public void testContainsKey() {
        assertTrue( t.containsKey("Red"), "contains key");
        assertTrue( t.containsKey("Black"), "contains key");
        assertTrue( t.containsKey("Green"), "contains key");
        assertFalse( t.containsKey("Blue"), "contains key");
    }

    @Test
    public void testContainsValue() {
        assertTrue( t.containsValue("Turnout"), "contains key");
        assertTrue( t.containsValue("Light"), "contains key");
        assertTrue( t.containsValue("Signal head"), "contains key");
        assertTrue( t.containsValue("Sensor"), "contains key");
        assertTrue( t.containsValue("Logix"), "contains key");
        assertFalse( t.containsValue("Green"), "contains key");
        assertFalse( t.containsValue("Red"), "contains key");
        assertFalse( t.containsValue("Black"), "contains key");
    }

    @Test
    public void testGet() {
        // Test exceptions
        expectException(() -> {
            t.get("abc");
        }, UnsupportedOperationException.class, "Not supported");
    }

    @Test
    public void testGetAll() {
        List<String> list = t.getAll("Green");
        assertEquals( 1, list.size(), "size is correct");
        assertFalse( list.isEmpty(), "list is not empty");
        assertEquals( "Sensor", list.get(0), "list[0] is correct");

        list = t.getAll("Red");
        assertEquals( 3, list.size(), "size is correct");
        assertFalse( list.isEmpty(), "list is not empty");
        assertEquals( "Turnout", list.get(0), "list[0] is correct");
        assertEquals( "Sensor", list.get(1), "list[1] is correct");
        assertEquals( "Light", list.get(2), "list[2] is correct");

        list = t.getAll("Black");
        assertEquals( 2, list.size(), "size is correct");
        assertFalse( list.isEmpty(), "list is not empty");
        assertEquals( "Signal head", list.get(0), "list[0] is correct");
        assertEquals( "Logix", list.get(1), "list[1] is correct");

        list = t.getAll("Blue");
        assertTrue( list.isEmpty(), "list is empty");
    }

    @Test
    public void testPut() {
        assertEquals( 6, t.size(), "size is correct");
        // Add values that already exists in the map
        t.put("Red", "Sensor");
        t.put("Red", "Light");
        t.put("Green", "Sensor");
        assertEquals( 6, t.size(), "size is correct");
    }

    @Test
    public void testPutAll() {
        // Test exceptions
        expectException(() -> {
            t.putAll(null);
        }, UnsupportedOperationException.class, "Not supported");
    }

    @Test
    public void testRemove() {
        // Test exceptions
        expectException(() -> {
            t.remove(null);
        }, UnsupportedOperationException.class, "Not supported");
    }

    @Test
    public void testRemoveValue() {
        assertEquals( 6, t.size(), "size is correct");
        t.removeValue("Red", "Sensor");
        assertEquals( 5, t.size(), "size is correct");
        t.removeValue("Red", "Sensor");
        assertEquals( 5, t.size(), "size is correct");
        t.removeValue("Green", "Sensor");
        assertEquals( 4, t.size(), "size is correct");
        t.removeValue("Unknown key", "Sensor");
        assertEquals( 4, t.size(), "size is correct");
    }

    @Test
    public void testClear() {
        assertEquals( 6, t.size(), "size is correct");
        assertFalse( t.isEmpty(), "map is not empty");
        t.clear();
        assertEquals( 0, t.size(), "size is correct");
        assertTrue( t.isEmpty(), "map is empty");
    }

    @Test
    public void testGetKeySet() {
        Set<String> set = t.keySet();
        assertEquals( 3, set.size(), "size is correct");
        assertFalse( set.isEmpty(), "set is not empty");
        assertTrue( set.contains("Red"), "set contains value");
        assertTrue( set.contains("Green"), "set contains value");
        assertTrue( set.contains("Black"), "set contains value");
    }

    @Test
    public void testValues() {
        Collection<String> collection = t.values();
        assertEquals( 6, collection.size(), "size is correct");
        assertFalse( collection.isEmpty(), "collection is not empty");
        assertTrue( collection.contains("Turnout"), "collection contains value");
        assertTrue( collection.contains("Sensor"), "collection contains value");
        assertTrue( collection.contains("Light"), "collection contains value");
        assertTrue( collection.contains("Signal head"), "collection contains value");
        assertTrue( collection.contains("Logix"), "collection contains value");
    }

    @Test
    public void testEntrySet() {
        // Test exceptions
        expectException(() -> {
            t.entrySet();
        }, UnsupportedOperationException.class, "Not supported");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        t = new DuplicateKeyMap<>();
        assertEquals( 0, t.size(), "size is correct");
        assertTrue( t.isEmpty(), "map is empty");
        t.put("Red", "Turnout");
        t.put("Red", "Sensor");
        t.put("Red", "Light");
        t.put("Green", "Sensor");
        t.put("Black", "Signal head");
        t.put("Black", "Logix");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DuplicateKeyMapTest.class);
}
