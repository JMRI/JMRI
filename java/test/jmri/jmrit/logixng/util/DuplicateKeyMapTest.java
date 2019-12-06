package jmri.jmrit.logixng.util;

import java.util.List;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test ReferenceUtil
 * 
 * @author Daniel Bergqvist 2019
 */
public class DuplicateKeyMapTest {

    private DuplicateKeyMap<String,String> t;
    
    private void expectException(Runnable r, Class<? extends Exception> exceptionClass, String errorMessage) {
        boolean exceptionThrown = false;
        try {
            r.run();
        } catch (Exception e) {
            if (e.getClass() != exceptionClass) {
                log.error("Expected exception {}, found exception {}",
                        e.getClass().getName(), exceptionClass.getName());
            }
            Assert.assertTrue("Exception is correct", e.getClass() == exceptionClass);
            Assert.assertEquals("Exception message is correct", errorMessage, e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception is thrown", exceptionThrown);
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("not null", t);
    }
    
    @Test
    public void testSize() {
        Assert.assertTrue("size is correct", 6 == t.size());
        Assert.assertFalse("map is not empty", t.isEmpty());
        
        DuplicateKeyMap<String,String> t2 = new DuplicateKeyMap<>();
        Assert.assertTrue("size is correct", 0 == t2.size());
        Assert.assertTrue("map is empty", t2.isEmpty());
    }
    
    @Test
    public void testContainsKey() {
        Assert.assertTrue("contains key", t.containsKey("Red"));
        Assert.assertTrue("contains key", t.containsKey("Black"));
        Assert.assertTrue("contains key", t.containsKey("Green"));
        Assert.assertFalse("contains key", t.containsKey("Blue"));
    }
    
    @Test
    public void testContainsValue() {
        Assert.assertTrue("contains key", t.containsValue("Turnout"));
        Assert.assertTrue("contains key", t.containsValue("Light"));
        Assert.assertTrue("contains key", t.containsValue("Signal head"));
        Assert.assertTrue("contains key", t.containsValue("Sensor"));
        Assert.assertTrue("contains key", t.containsValue("Logix"));
        Assert.assertFalse("contains key", t.containsValue("Green"));
        Assert.assertFalse("contains key", t.containsValue("Red"));
        Assert.assertFalse("contains key", t.containsValue("Black"));
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
        Assert.assertTrue("size is correct", 1 == list.size());
        Assert.assertFalse("list is not empty", list.isEmpty());
        Assert.assertTrue("list[0] is correct", "Sensor".equals(list.get(0)));
        
        list = t.getAll("Red");
        Assert.assertTrue("size is correct", 3 == list.size());
        Assert.assertFalse("list is not empty", list.isEmpty());
        Assert.assertTrue("list[0] is correct", "Turnout".equals(list.get(0)));
        Assert.assertTrue("list[1] is correct", "Sensor".equals(list.get(1)));
        Assert.assertTrue("list[2] is correct", "Light".equals(list.get(2)));
        
        list = t.getAll("Black");
        Assert.assertTrue("size is correct", 2 == list.size());
        Assert.assertFalse("list is not empty", list.isEmpty());
        Assert.assertTrue("list[0] is correct", "Signal head".equals(list.get(0)));
        Assert.assertTrue("list[1] is correct", "Logix".equals(list.get(1)));
        
        list = t.getAll("Blue");
        Assert.assertTrue("list is empty", list.isEmpty());
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
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        t = new DuplicateKeyMap<>();
        Assert.assertTrue("size is correct", 0 == t.size());
        Assert.assertTrue("map is empty", t.isEmpty());
        t.put("Red", "Turnout");
        t.put("Red", "Sensor");
        t.put("Red", "Light");
        t.put("Green", "Sensor");
        t.put("Black", "Signal head");
        t.put("Black", "Logix");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private final static Logger log = LoggerFactory.getLogger(DuplicateKeyMapTest.class);
}
