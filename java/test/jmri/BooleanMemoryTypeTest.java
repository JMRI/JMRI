package jmri;

import org.junit.*;

/**
 * Tests for the StringMemoryType class
 *
 * @author	Daniel Bergqvist Copyright (C) 2019
 */
public class BooleanMemoryTypeTest {

    @Test
    public void testBooleanMemoryType() throws JmriException {
        BooleanMemoryType booleanMemoryType = new BooleanMemoryType();
        booleanMemoryType.setInitialValue(true);
        Assert.assertTrue("BooleanMemoryType has expected name",
                "Boolean".equals(booleanMemoryType.getName()));
        
        Memory m = InstanceManager.getDefault(MemoryManager.class).newMemory("IM1");
        m.setMemoryType(booleanMemoryType);
        Assert.assertTrue("Memory has expected initial value", (boolean) m.getValue());
        Assert.assertTrue("Memory has expected format string value",
                "On".equals(m.getMemoryType().formatString(m.getValue())));

        m.setValue(false);
        Assert.assertTrue("Memory has expected value", ! (boolean) m.getValue());
        Assert.assertTrue("Memory has expected format string value",
                "Off".equals(m.getMemoryType().formatString(m.getValue())));

        m.setValue(true);
        Assert.assertTrue("Memory has expected value", (boolean) m.getValue());
        Assert.assertTrue("Memory has expected format string value",
                "On".equals(m.getMemoryType().formatString(m.getValue())));

        // expect this to throw exception wrong value
        boolean threw = false;
        try {
            m.setValue(10);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("Type java.lang.Integer cannot be stored in a Memory of type Boolean")) {
                threw = true;
            } else {
                Assert.fail("Failed to set Memory due to wrong reason: " + ex);
            }
        }
        Assert.assertTrue("Expected exception", threw);

        // expect this to throw exception wrong value
        threw = false;
        try {
            m.setValue(2.5);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("Type java.lang.Double cannot be stored in a Memory of type Boolean")) {
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
            if (ex.getMessage().equals("Type java.lang.Object cannot be stored in a Memory of type Boolean")) {
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
