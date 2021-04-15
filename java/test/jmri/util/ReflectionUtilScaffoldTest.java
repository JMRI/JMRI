package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ReflectionUtilScaffold.
 * 
 * @author Daniel Bergqvist 2019
 */
public class ReflectionUtilScaffoldTest {

    public class MyClass {
        private String myField = "Hello World";
    }

    @Test
    public void testReflection() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        MyClass mc = new MyClass();
        Assert.assertEquals("Test", "Hello World", mc.myField);
        jmri.util.ReflectionUtilScaffold.setField(mc, "myField", "A new value");
        Assert.assertEquals("Test", "A new value", mc.myField);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
