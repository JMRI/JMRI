package jmri;

import org.junit.*;

/**
 * Tests for the StringMemoryType class
 *
 * @author	Daniel Bergqvist Copyright (C) 2019
 */
public class FloatMemoryTypeTest {

    @Test
    public void testFloatMemoryType() throws JmriException {
        FloatMemoryType floatMemoryType = new FloatMemoryType();
        floatMemoryType.setInitialValue((float)3.45);
        Assert.assertTrue("FloatMemoryType has expected name",
                "Float".equals(floatMemoryType.getName()));
        
        Memory m = InstanceManager.getDefault(MemoryManager.class).newMemory("IM1");
        m.setMemoryType(floatMemoryType);
        Assert.assertTrue("Memory has expected initial value",
                (new Float(3.45)).equals(m.getValue()));

        floatMemoryType.setNumDecimals(1);
        m.setValue(new Float(2.12345678));
        Assert.assertTrue("Memory has expected value",
                (new Float(2.12345678)).equals(m.getValue()));
        Assert.assertTrue("Memory has expected format string value",
                "2.1".equals(m.getMemoryType().formatString(m)));

        floatMemoryType.setNumDecimals(3);
        m.setValue(new Float(2.12345678));
        Assert.assertTrue("Memory has expected value",
                (new Float(2.12345678)).equals(m.getValue()));
        Assert.assertTrue("Memory has expected format string value",
                "2.123".equals(m.getMemoryType().formatString(m)));

        // expect this to throw exception wrong value
        boolean threw = false;
        try {
            m.setValue(10);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("Type java.lang.Integer cannot be stored in a Memory of type String")) {
                threw = true;
            } else {
                Assert.fail("Failed to set Memory due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception", threw);

        // expect this to throw exception wrong value
        threw = false;
        try {
            m.setValue("A string");
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("Type java.lang.Double cannot be stored in a Memory of type String")) {
                threw = true;
            } else {
                Assert.fail("Failed to set Memory due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception", threw);

        // expect this to throw exception wrong value
        threw = false;
        try {
            m.setValue(new Object());
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("Type java.lang.Object cannot be stored in a Memory of type String")) {
                threw = true;
            } else {
                Assert.fail("Failed to set Memory due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception", threw);
    }

    @Before
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

}
