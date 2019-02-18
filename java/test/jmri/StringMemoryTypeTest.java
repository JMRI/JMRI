package jmri;

import org.junit.*;

/**
 * Tests for the StringMemoryType class
 *
 * @author	Daniel Bergqvist Copyright (C) 2019
 */
public class StringMemoryTypeTest {

    @Test
    public void testStringMemoryType() throws JmriException {
        StringMemoryType stringMemoryType = new StringMemoryType();
        stringMemoryType.setInitialValue("Some string");
        Assert.assertTrue("StringMemoryType has expected name",
                "String".equals(stringMemoryType.getName()));
        
        Memory m = InstanceManager.getDefault(MemoryManager.class).newMemory("IM1");
        m.setMemoryType(stringMemoryType);
        Assert.assertTrue("Memory has expected initial value", "Some string".equals(m.getValue()));

        stringMemoryType.setMaxLenght(10);
        m.setValue("A string");
        Assert.assertTrue("Memory has expected value", "A string".equals(m.getValue()));

        stringMemoryType.setMaxLenght(10);
        m.setValue("A long string");
        Assert.assertTrue("Memory has expected value", "A long str".equals(m.getValue()));

        stringMemoryType.setMaxLenght(0);
        m.setValue("A long string");
        Assert.assertTrue("Memory has expected value", "A long string".equals(m.getValue()));

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
            m.setValue(2.5);
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
